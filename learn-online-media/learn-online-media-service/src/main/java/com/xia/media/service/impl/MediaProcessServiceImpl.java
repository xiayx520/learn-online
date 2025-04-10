package com.xia.media.service.impl;

import com.xia.media.mapper.MediaFilesMapper;
import com.xia.media.mapper.MediaProcessHistoryMapper;
import com.xia.media.mapper.MediaProcessMapper;
import com.xia.media.model.po.MediaFiles;
import com.xia.media.model.po.MediaProcess;
import com.xia.media.model.po.MediaProcessHistory;
import com.xia.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MediaProcessServiceImpl implements MediaProcessService {


    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

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

    /**
     * 保存任务处理结果
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //根据任务id查询任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }
        //任务处理失败
        if ("3".equals(status)) {
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            log.debug("更新任务失败记录,{}", mediaProcess);
            return;
        }
        //任务处理成功
        if ("2".equals(status)) {
            //更新任务记录
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            //更新文件表记录
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            if(mediaFiles!=null){
                //更新媒资文件中的访问url
                mediaFiles.setUrl(url);
                mediaFilesMapper.updateById(mediaFiles);
            }
            //添加到文件表历史记录表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);
            //删除待处理任务记录
            mediaProcessMapper.deleteById(mediaProcess.getId());
        }

    }
}
