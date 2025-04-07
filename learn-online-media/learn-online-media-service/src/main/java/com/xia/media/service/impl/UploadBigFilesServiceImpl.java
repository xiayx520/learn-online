package com.xia.media.service.impl;

import com.xia.base.model.RestResponse;
import com.xia.media.mapper.MediaFilesMapper;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.service.UploadBigFilesService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class UploadBigFilesServiceImpl implements UploadBigFilesService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

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
}
