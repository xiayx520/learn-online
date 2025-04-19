package com.xia.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xia.base.exception.GlobalException;
import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.base.model.RestResponse;
import com.xia.media.mapper.MediaFilesMapper;
import com.xia.media.mapper.MediaProcessMapper;
import com.xia.media.model.dto.QueryMediaParamsDto;
import com.xia.media.model.dto.UploadFileParamsDto;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.model.po.MediaProcess;
import com.xia.media.model.vo.UploadFileResultVO;
import com.xia.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFileService currentProxy;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename())
                .eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType())
                .eq(StringUtils.isNotEmpty(queryMediaParamsDto.getAuditStatus()), MediaFiles::getAuditStatus, queryMediaParamsDto.getAuditStatus());
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * 上传文件
     *
     * @param companyId
     * @param file
     * @return
     */
    @Override
    public UploadFileResultVO uploadFile(Long companyId, MultipartFile file, String objectName) throws IOException {
        //构建文件信息对象
        UploadFileParamsDto uploadFileParamsDto = UploadFileParamsDto.builder()
                .filename(file.getOriginalFilename())
                .fileType("001001")//文件类型:"code":"001001","desc":"图片"
                .fileSize(file.getSize())
                .build();
        //创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        //将文件内容写入临时文件
        file.transferTo(tempFile);
        //临时文件路径
        String filePath = tempFile.getAbsolutePath();
        //获取文件md5
        String fileMd5 = getFileMd5(tempFile);
        //获取文件扩展名
        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //获取objectName
        if(StringUtils.isEmpty(objectName))
        {
            objectName = getObjectName(fileMd5, extension);
        }
        //上传文件
        addMediaFilesToMinIO(bucket, objectName, filePath, getMimeType(extension));
        //记录文件信息到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(objectName, fileMd5, companyId, uploadFileParamsDto, bucket);
        //删除临时文件
        tempFile.delete();
        //返回上传的结果
        UploadFileResultVO uploadFileResultVO = new UploadFileResultVO();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultVO);
        //返回上传的结果
        return uploadFileResultVO;
    }

    //添加文件信息到数据库
    @Transactional
    public MediaFiles addMediaFilesToDB(String objectName, String fileMd5, Long companyId, UploadFileParamsDto uploadFileParamsDto, String bucket) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //判断文件是否存在
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 1) {
                throw new GlobalException("文件信息写入数据库失败");
            }
            log.debug("向数据库写入文件记录成功,{}", mediaFiles);
            //添加到待处理任务表
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    public void addWaitingTask(MediaFiles mediaFiles) {
        String fileName = mediaFiles.getFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setFailCount(0);
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcessMapper.insert(mediaProcess);
            log.debug("向待处理任务表写入记录成功,{}", mediaProcess);
        }
    }

    //获取文件类型
    public String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 根据文件扩展名获取媒体类型
        ContentInfo info = ContentInfoUtil.findExtensionMatch(extension);
        return info == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : info.getMimeType();
    }

    //获取objectName
    private String getObjectName(String fileMd5, String extension) {
        // 以日期命名文件名,获取当日的日期
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 文件名：2025/4/7/文件md5值.文件扩展名
        date = date.replace("-", "/");
        return date + "/" + fileMd5 + extension;
    }
    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @Override
    public RestResponse<String> getPlayUrl(String mediaId) {
        if (StringUtils.isBlank(mediaId)) {
            return RestResponse.validfail("文件id为空", "false");
        }
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        if (mediaFiles == null) {
            return RestResponse.validfail("文件不存在", "false");
        }

        String url = mediaFiles.getUrl();
        if (StringUtils.isBlank(url)) {
            return RestResponse.validfail("文件路径为空", "false");
        }

        return RestResponse.success(url);
    }
}
