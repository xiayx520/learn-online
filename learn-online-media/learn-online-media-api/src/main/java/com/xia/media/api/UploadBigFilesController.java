package com.xia.media.api;

import com.xia.base.model.RestResponse;
import com.xia.media.service.UploadBigFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @param chunkIndex
     * @return
     */
    @ApiOperation("检查分块文件是否存在")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(String fileMd5, Integer chunkIndex) {
        log.info("检查分块文件是否存在,fileMd5:{},chunk:{}", fileMd5, chunkIndex);
        return uploadBigFilesService.checkChunk(fileMd5, chunkIndex);
    }
}
