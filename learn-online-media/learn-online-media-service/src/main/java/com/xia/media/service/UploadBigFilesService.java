package com.xia.media.service;

import com.xia.base.model.RestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 大文件上传服务
 */
public interface UploadBigFilesService {

    /**
     * 检查文件是否存在
     * @param fileMd5
     * @return
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块文件是否存在
     * @param fileMd5
     * @param chunkIndex
     * @return
     */
    RestResponse<Boolean> checkChunk(String fileMd5, Integer chunkIndex);

    /**
     * 上传分块文件
     * @param file
     * @param fileMd5
     * @param chunkIndex
     * @return
     */
    RestResponse<Boolean> uploadChunk(MultipartFile file, String fileMd5, Integer chunkIndex) throws IOException;
}
