package com.xia.media.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xia.base.exception.GlobalException;
import com.xia.base.model.RestResponse;
import com.xia.media.mapper.MediaFilesMapper;
import com.xia.media.model.dto.UploadFileParamsDto;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.service.MediaFileService;
import com.xia.media.service.UploadBigFilesService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UploadBigFilesServiceImpl implements UploadBigFilesService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    /**
     * 检查文件是否存在
     *
     * @param fileMd5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //检查文件是否存在
        if (fileMd5 == null) {
            return RestResponse.validfail("文件md5为空");
        }
        //检查文件信息表
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();

            //判断文件在minio是否存在
            try {
                InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(filePath)
                                .build());
                if (inputStream != null) {
                    //关闭流
                    inputStream.close();
                    return RestResponse.validfail(true, "文件已存在");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }


    /**
     * 检查分块文件是否存在
     *
     * @param fileMd5
     * @param chunkIndex
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, Integer chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        try {
            InputStream fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket_videoFiles)
                            .object(chunkFilePath)
                            .build());

            if (fileInputStream != null) {
                fileInputStream.close();
                //分块已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块未存在
        return RestResponse.success(false);
    }

    /**
     * 上传分块文件
     *
     * @param file
     * @param fileMd5
     * @param chunkIndex
     * @return
     */
    @Override
    public RestResponse<Boolean> uploadChunk(MultipartFile file, String fileMd5, Integer chunkIndex) throws IOException {
        //上传的文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunkIndex;
        File chunkFile = File.createTempFile("minio", "temp");
        file.transferTo(chunkFile);
        //本地的文件路径
        String tempFilePath = chunkFile.getAbsolutePath();
        //上传文件到minio
        addMediaFilesToMinIO(bucket_videoFiles, chunkFilePath, tempFilePath, getMimeType(null));
        //删除临时文件
        chunkFile.delete();
        return RestResponse.success(true);
    }

    /**
     * 合并分块文件
     *
     * @param companyId
     * @param fileMd5
     * @param fileName
     * @param chunkTotal
     * @return
     */
    @Override
    public RestResponse<Boolean> mergechunks(Long companyId, String fileMd5, String fileName, int chunkTotal) {
        if (fileMd5.isEmpty() || fileName.isEmpty() || chunkTotal < 1) {
            return RestResponse.validfail("参数不合法");
        }
        UploadFileParamsDto uploadFileParamsDto = UploadFileParamsDto.builder()
                .fileType("001002")
                .filename(fileName)
                .tags("课程视频")
                .remark("")
                .build();
        //获取分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //获取需要合并文件的具体路径，拼接分块文件路径
        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucket_videoFiles)
                    .object(chunkFilePath)
                    .build();
            sources.add(composeSource);
        }
        //合并文件的路径 md5.后缀名
        String mergeFilePath = chunkFileFolderPath + fileMd5 + fileName.substring(fileName.lastIndexOf("."));
        //合并文件
        try {
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket_videoFiles)
                            .object(mergeFilePath)
                            .sources(sources)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误原因:{}", bucket_videoFiles, fileName, e.getMessage(), e);
            return RestResponse.validfail(false, "合并文件出错");
        }
        //比较文件的MD5值，如果一致则删除分块文件
        //下载合并后的文件
        File minioFile = downloadFileFromMinIO(bucket_videoFiles, mergeFilePath);
        if (minioFile == null) {
            log.debug("下载合并后文件失败,mergeFilePath:{}", mergeFilePath);
            return RestResponse.validfail(false, "下载合并后文件失败。");
        }

        try (InputStream newFileInputStream = new FileInputStream(minioFile)) {
            //minio上文件的md5值
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            //比较md5值，不一致则说明文件不完整
            if (!fileMd5.equals(md5Hex)) {
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(minioFile.length());
        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        } finally {
            if (minioFile != null) {
                minioFile.delete();
            }
        }

        //写入数据库
        mediaFileService.addMediaFilesToDB(mergeFilePath, fileMd5, companyId, uploadFileParamsDto, bucket_videoFiles);
        //删除分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        //返回成功
        return RestResponse.success(true);
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath
     * @param chunkTotal
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        //获取分块文件路径
        List<DeleteObject> deleteObjectList = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            deleteObjectList.add(new DeleteObject(chunkFilePath));
        }
        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucket_videoFiles)
                            .objects(deleteObjectList)
                            .build()
            );
            for (Result<DeleteError> result : results) {
                DeleteError deleteError = result.get();
                log.error("删除分块文件失败,bucket:{},objectName:{},错误原因:{}", bucket_videoFiles, deleteError.objectName(), deleteError.message(), deleteError.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除分块文件失败,bucket:{},objectName:{},错误原因:{}", bucket_videoFiles, chunkFileFolderPath, e.getMessage(), e);
        }
    }

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            stream.close();
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    public void addMediaFilesToMinIO(String bucket, String objectName, String filePath, String mimeType) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(filePath)
                            .contentType(mimeType)
                            .build()
            );
            log.info("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
            throw new GlobalException("上传文件到文件系统失败");
        }
    }

    public String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 根据文件扩展名获取媒体类型
        ContentInfo info = ContentInfoUtil.findExtensionMatch(extension);
        return info == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : info.getMimeType();
    }
}
