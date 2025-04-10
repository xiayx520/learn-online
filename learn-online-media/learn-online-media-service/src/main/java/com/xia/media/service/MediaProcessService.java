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
}
