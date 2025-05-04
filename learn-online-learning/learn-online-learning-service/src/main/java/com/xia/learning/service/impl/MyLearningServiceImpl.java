package com.xia.learning.service.impl;

import com.xia.base.model.RestResponse;
import com.xia.content.model.po.CoursePublish;
import com.xia.learning.feignclient.ContentServiceClient;
import com.xia.learning.feignclient.MediaServiceClient;
import com.xia.learning.model.vo.XcCourseTablesVO;
import com.xia.learning.service.MyLearningService;
import com.xia.learning.service.XcChooseCourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class MyLearningServiceImpl implements MyLearningService {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private XcChooseCourseService xcChooseCourseService;


    /**
     * 获取视频
     * @param userId
     * @param courseId
     * @param teachplanId
     * @param mediaId
     * @return
     */
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        if(mediaId == null || mediaId.isEmpty() || courseId == null || courseId == 0 || teachplanId == null || teachplanId == 0) {
            log.error("getVideo:{}", "参数异常");
        }
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            RestResponse.validfail("课程不存在");
        }
        //判断该章节是否可以试看
        Boolean isPreview = contentServiceClient.isPreview(teachplanId);
        if (isPreview) {
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        //用户已登录
        if (StringUtils.isNotEmpty(userId)) {
            //取学习资格
            XcCourseTablesVO learningStatus = xcChooseCourseService.getLearningStatus(userId, courseId);
            //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = learningStatus.getLearnStatus();
            if (learnStatus.equals("702001")) {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702002")) {
                return RestResponse.validfail("无法观看，由于没有选课或选课后没有支付");
            } else if (learnStatus.equals("702003")) {
                return RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }
        //未登录或未选课判断是否收费
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")) {//免费可以正常学习
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validfail("请购买课程后继续学习");
    }
}
