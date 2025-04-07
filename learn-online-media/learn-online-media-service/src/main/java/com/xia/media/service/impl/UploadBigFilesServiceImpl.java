package com.xia.media.service.impl;

import com.xia.base.model.RestResponse;
import com.xia.media.mapper.MediaFilesMapper;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.service.UploadBigFilesService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
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

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }
}
