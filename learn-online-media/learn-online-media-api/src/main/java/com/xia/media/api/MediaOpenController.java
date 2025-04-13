package com.xia.media.api;


import com.xia.base.model.RestResponse;
import com.xia.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "媒资管理开放接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    private MediaFileService mediaFileService;


    /**
     * 获取播放地址
     * @param mediaId
     * @return
     */
    @GetMapping("/preview/{mediaId}")
    @ApiOperation("获取播放地址")
    public RestResponse<String> getPlayUrl(@PathVariable String mediaId) {
        return mediaFileService.getPlayUrl(mediaId);
    }
}
