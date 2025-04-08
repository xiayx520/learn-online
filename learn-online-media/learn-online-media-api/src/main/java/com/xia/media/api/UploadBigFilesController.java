package com.xia.media.api;

import com.xia.base.model.RestResponse;
import com.xia.media.service.UploadBigFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@Api(value = "大文件上传接口", tags = "大文件上传接口")
public class UploadBigFilesController {

    @Autowired
    private UploadBigFilesService uploadBigFilesService;

    /**
     * 检查文件是否存在
     * @param fileMd5
     * @return
     */
    @ApiOperation("检查文件是否存在")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(String fileMd5) {

        return uploadBigFilesService.checkFile(fileMd5);
    }


    /**
     * 检查分块文件是否存在
     * @param fileMd5
     * @param chunk
     * @return
     */
    @ApiOperation("检查分块文件是否存在")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(String fileMd5, Integer chunk) {
        log.info("检查分块文件是否存在,fileMd5:{},chunk:{}", fileMd5, chunk);
        return uploadBigFilesService.checkChunk(fileMd5, chunk);
    }

    /**
     * 上传分块文件
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    @ApiOperation("上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse<Boolean> uploadChunk(MultipartFile file, String fileMd5, Integer chunk) throws IOException {
        log.info("上传分块文件,fileMd5:{},chunk:{}", fileMd5, chunk);
        return uploadBigFilesService.uploadChunk(file, fileMd5, chunk);
    }


    /**
     * 合并文件
     * @param fileMd5
     * @param fileName
     * @param chunkTotal
     * @return
     */
    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse<Boolean> mergechunks(String fileMd5, String fileName, int chunkTotal){
        Long companyId = 1232141425L;
        log.info("合并文件,fileMd5:{},fileName:{},chunkTotal:{}", fileMd5, fileName, chunkTotal);
        return uploadBigFilesService.mergechunks(companyId, fileMd5, fileName, chunkTotal);
    }
}
