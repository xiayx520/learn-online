package com.xia.media.service.impl;

import com.xia.media.mapper.MediaProcessMapper;
import com.xia.media.model.po.MediaProcess;
import com.xia.media.service.MediaProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaProcessServiceImpl implements MediaProcessService {


    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    /**
     * 根据分片参数获取待处理任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);
    }

    /**
     * 启动任务
     * @param taskId
     * @return
     */
    @Override
    public Boolean startTask(long taskId) {
        int i = mediaProcessMapper.updateTaskId(taskId);
        return i > 0;
    }
}
