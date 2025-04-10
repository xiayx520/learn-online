package com.xia.media.jobhandler;

import com.xia.base.utils.Mp4VideoUtil;
import com.xia.media.model.po.MediaProcess;
import com.xia.media.service.MediaFileService;
import com.xia.media.service.MediaProcessService;
import com.xia.media.service.UploadBigFilesService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoJob {

    @Autowired
    private MediaProcessService mediaProcessService;
    @Autowired
    private MediaFileService mediaFileService;
    @Autowired
    private UploadBigFilesService uploadBigFilesService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;


    @XxlJob("videoJobHandler")
    public void videoJobHandler() {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        log.info("开始执行第{}批任务", shardIndex);
        // 获取需要处理的任务
        int processors = Runtime.getRuntime().availableProcessors();
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        // 没有任务，结束
        if (mediaProcessList == null || mediaProcessList.isEmpty()) {
            log.info("没有需要处理的任务，任务结束");
            return;
        }
        // 任务数
        int size = mediaProcessList.size();
        log.info("查询到{}条需要处理的任务，开始执行", size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 创建线程池，启动size个线程的线程池
        Executor threadPool = Executors.newFixedThreadPool(size);
        // 遍历任务集合，提交到线程池中执行
        for (MediaProcess mediaProcess : mediaProcessList) {
            threadPool.execute(
                    () -> {
                        try {
                            //任务id
                            Long taskId = mediaProcess.getId();
                            boolean startTask = mediaProcessService.startTask(taskId);
                            if (!startTask) {
                                log.info("未抢到任务，任务ID:{}", taskId);
                                return;
                            }
                            log.info("开始执行任务，任务ID:{}", taskId);
                            // 执行任务
                            // 获取桶
                            String bucket = mediaProcess.getBucket();
                            // 获取文件id
                            String fileId = mediaProcess.getFileId();
                            // 获取文件路径
                            String filePath = mediaProcess.getFilePath();
                            // 下载文件
                            File originalFile = uploadBigFilesService.downloadFileFromMinIO(bucket, filePath);
                            if (originalFile == null) {
                                log.debug("下载待处理文件失败, originalFile:{}", bucket.concat(filePath));
                                mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载待处理文件失败");
                                return;
                            }
                            //处理下载的视频文件
                            File mp4File = null;
                            try {
                                mp4File = File.createTempFile("mp4", ".mp4");
                            } catch (IOException e) {
                                log.error("创建mp4临时文件失败");
                                mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建mp4临时文件失败");
                                return;
                            }
                            // 转码
                            String result = null;
                            try {
                                Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                                result = mp4VideoUtil.generateMp4();
                            } catch (Exception e) {
                                log.error("视频转码过程异常,视频路径:{}", originalFile.getAbsolutePath(), e);
                                mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码过程异常");
                                return;
                            }
                            if (!"success".equals(result)) {
                                log.error("视频转码失败,视频地址:{},错误信息:{}", bucket + '/' + filePath, result);
                                mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                                return;
                            }
                            // 上传到minio
                            String objectName = getFilePath(fileId, ".mp4");
                            //访问url
                            String url = "/" + bucket + "/" + objectName;
                            try {
                                mediaFileService.addMediaFilesToMinIO(bucket, objectName, mp4File.getAbsolutePath(), "video/mp4");
                                mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                            } catch (Exception e) {
                                log.error("处理后视频上传或入库失败,bucket:{},objectName:{}", bucket, objectName, e);
                                mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "处理后视频上传minio或入库失败");
                                return;
                            }
                        } catch (Exception e) {
                            mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, e.getMessage());
                            log.error("执行任务出错，任务ID:{}", mediaProcess.getId(), e);
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
        }
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        try {
            countDownLatch.await(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("任务中断");
        }
    }

    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

}
