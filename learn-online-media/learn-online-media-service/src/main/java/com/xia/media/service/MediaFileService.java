package com.xia.media.service;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.base.model.RestResponse;
import com.xia.media.model.dto.QueryMediaParamsDto;
import com.xia.media.model.dto.UploadFileParamsDto;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.model.vo.UploadFileResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * @description 上传文件
     * @param companyId
     * @param file
     * @return
     */
    UploadFileResultVO uploadFile(Long companyId, MultipartFile file, String objectName) throws IOException;


    /**
     * @description 将文件信息添加到文件表
     * @param objectName
     * @param fileMd5
     * @param companyId
     * @param uploadFileParamsDto
     * @return
     */
    MediaFiles addMediaFilesToDB(String objectName, String fileMd5, Long companyId, UploadFileParamsDto uploadFileParamsDto, String bucket);

    /**
     * @description 将文件信息上传到minio
     * @param bucket
     * @param objectName
     * @param filePath
     * @param mimeType
     */
    void addMediaFilesToMinIO(String bucket, String objectName, String filePath, String mimeType);


    /**
     * @description 根据md5值查询文件
     * @param mediaId
     * @return
     */
    RestResponse<String> getPlayUrl(String mediaId);
}
