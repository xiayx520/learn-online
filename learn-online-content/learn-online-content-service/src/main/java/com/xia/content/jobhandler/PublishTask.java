package com.xia.content.jobhandler;

import com.xia.messagesdk.model.po.MqMessage;
import com.xia.messagesdk.service.MessageProcessAbstract;
import com.xia.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PublishTask extends MessageProcessAbstract {

    @Autowired
    private MqMessageService mqMessageService;

    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //课程静态化,上传minio
        generateCourseHtml(mqMessage, courseId);
        //课程索引es
        saveCourseIndex(mqMessage, courseId);
        //课程缓存redis
        saveCourseCache(mqMessage, courseId);
        return true;
    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息,课程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("生成课程静态化页面,课程id:{}", courseId);
        Long taskId = mqMessage.getId();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("生成课程静态化页面,课程id:{},任务已处理,无需处理", courseId);
            return;
        }
        //生成课程静态化页面
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //更新任务状态1
        mqMessageService.completedStageOne(taskId);
    }


    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        int processors = Runtime.getRuntime().availableProcessors();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", processors, 60);
    }
}
