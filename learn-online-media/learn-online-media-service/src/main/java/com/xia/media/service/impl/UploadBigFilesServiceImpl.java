package com.xia.media.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xia.base.exception.GlobalException;
import com.xia.base.model.RestResponse;
import com.xia.media.mapper.MediaFilesMapper;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.service.UploadBigFilesService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class UploadBigFilesServiceImpl implements UploadBigFilesService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    /**
     * 检查文件是否存在
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
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }


    /**
     * 检查分块文件是否存在
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
