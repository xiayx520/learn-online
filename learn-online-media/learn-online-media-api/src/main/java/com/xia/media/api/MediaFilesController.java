package com.xia.media.api;


import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.media.model.dto.QueryMediaParamsDto;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.model.vo.UploadFileResultVO;
import com.xia.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

    }

    /**
     * 上传文件
     * @param file
     * @return
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload/coursefile")
    public UploadFileResultVO uploadFile(@RequestPart("filedata") MultipartFile file, @RequestParam(value= "objectName",required=false)String objectName) throws IOException {
        Long companyId = 1232141425L;
        return mediaFileService.uploadFile(companyId, file, objectName);
    }

}
