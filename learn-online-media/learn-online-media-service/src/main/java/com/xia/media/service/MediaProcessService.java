package com.xia.media.service;

import com.xia.media.model.po.MediaProcess;

import java.util.List;

public interface MediaProcessService {

    /**
     * 根据分片参数获取待处理任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /**
     * 启动任务
     * @param taskId
     * @return
     */
    Boolean startTask(long taskId);


    /**
     * 任务处理完成需要更新任务处理结果，任务执行成功更新视频的URL、及任务处理结果，
     * 将待处理任务记录删除，同时向历史任务表添加记录。
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

}
