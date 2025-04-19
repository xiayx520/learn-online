package com.xia.content.jobhandler;

import com.xia.base.exception.GlobalException;
import com.xia.content.config.MultipartSupportConfig;
import com.xia.content.feignclient.MediaServiceClient;
import com.xia.content.model.vo.CoursePreviewVO;
import com.xia.content.service.CoursePublishService;
import com.xia.messagesdk.mapper.MqMessageMapper;
import com.xia.messagesdk.model.po.MqMessage;
import com.xia.messagesdk.service.MessageProcessAbstract;
import com.xia.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PublishTask extends MessageProcessAbstract {

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private CoursePublishService coursePublishService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private MqMessageMapper mqMessageMapper;

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

        //查询消息状态
        mqMessage = mqMessageMapper.selectById(mqMessage.getId());
        if (Objects.equals(mqMessage.getStageState1(), "1") && Objects.equals(mqMessage.getStageState2(), "1") && Objects.equals(mqMessage.getStageState3(), "1")) {
            return true;
        }
        return false;
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
            //生成课程静态化页面
            File file = generateCourseHtml(courseId);
            //上传课程静态化页面至minio
            //上传静态化页面
            if(file!=null){
                uploadCourseHtml(file, courseId);
            }
            //删除本地文件
            file.delete();
        } catch (Exception e) {
            log.error("生成课程静态化页面异常:{}", e.getMessage());
            throw new RuntimeException(e);
        }

        //更新任务状态1
        mqMessageService.completedStageOne(taskId);
    }

    private void uploadCourseHtml(File file, long courseId) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(course==null){
            throw new GlobalException("上传静态文件异常");
        }
    }


    //课程静态化
    public File generateCourseHtml(long courseId) {
        File htmlFile = null;
        //配置freemarker
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            //加载模板
            InputStream inputStream = this.getClass().getResourceAsStream("/templates/course_template.ftl");
            if (inputStream == null) {
                throw new FileNotFoundException("模板文件未找到");
            }
            Template template = new Template("course_template.ftl",
                    new InputStreamReader(inputStream, "UTF-8"), configuration);

            //准备数据
            CoursePreviewVO coursePreviewInfo = coursePublishService.getCoursePreviewVO(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            inputStream = IOUtils.toInputStream(content);
            htmlFile = File.createTempFile("course", ".html");
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);


        } catch (Exception e) {
            log.error("课程静态化异常:{}", e.getMessage());
            e.printStackTrace();
        }

        return htmlFile;
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
