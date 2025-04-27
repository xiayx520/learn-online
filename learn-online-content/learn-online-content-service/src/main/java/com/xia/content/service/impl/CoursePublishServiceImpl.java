package com.xia.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xia.base.exception.CommonError;
import com.xia.base.exception.GlobalException;
import com.xia.content.mapper.CourseBaseMapper;
import com.xia.content.mapper.CourseMarketMapper;
import com.xia.content.mapper.CoursePublishMapper;
import com.xia.content.mapper.CoursePublishPreMapper;
import com.xia.content.model.po.*;
import com.xia.content.model.vo.CourseBaseInfoVO;
import com.xia.content.model.vo.CoursePreviewVO;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.CourseBaseInfoService;
import com.xia.content.service.CoursePublishService;
import com.xia.content.service.CourseTeacherService;
import com.xia.content.service.TeachPlanService;
import com.xia.messagesdk.model.po.MqMessage;
import com.xia.messagesdk.service.MqMessageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CoursePublishServiceImpl implements CoursePublishService {


    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private CourseTeacherService courseTeacherService;

    @Autowired
    private TeachPlanService teachPlanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private MqMessageService mqMessageService;

    /**
     * 根据课程id查询课程发布信息
     * @param courseId
     * @return
     */
    @Override
    public CoursePreviewVO getCoursePreviewVO(Long courseId) {
        if(courseId == null) {
            throw new GlobalException("课程id为空");
        }
        CourseBaseInfoVO courseBaseInfoVO = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachPlanVO> teachPlanTreeNodes = teachPlanService.getTeachPlanTree(courseId);
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacher(courseId);

        CoursePreviewVO coursePreviewVO = new CoursePreviewVO();
        coursePreviewVO.setCourseBase(courseBaseInfoVO);
        coursePreviewVO.setTeachplans(teachPlanTreeNodes);
        coursePreviewVO.setCourseTeacher(courseTeachers);

        return coursePreviewVO;
    }


    /**
     * 课程提交审核
     * @param courseId
     */
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        if(courseId == null) {
            throw new GlobalException("课程id为空");
        }
        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();

        //当前审核状态为已提交不允许再次提交
        if("202003".equals(auditStatus)){
            throw new GlobalException("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            throw new GlobalException("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            throw new GlobalException("提交失败，请上传课程图片");
        }

        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息
        CourseBaseInfoVO courseBaseInfoVO = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfoVO,coursePublishPre);

        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转化为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        //课程计划
        List<TeachPlanVO> teachPlanTreeNodes = teachPlanService.getTeachPlanTree(courseId);
        if(teachPlanTreeNodes == null || teachPlanTreeNodes.isEmpty()){
            throw new GlobalException("提交失败，还没有添加课程计划");
        }
        String teachplan = JSON.toJSONString(teachPlanTreeNodes);
        coursePublishPre.setTeachplan(teachplan);

        //师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacher(courseId);
        if(courseTeachers == null || courseTeachers.isEmpty()){
            throw new GlobalException("提交失败，还没有添加师资信息");
        }
        //转化为json
        String courseTeachersJson = JSON.toJSONString(courseTeachers);
        coursePublishPre.setTeachers(courseTeachersJson);

        //机构id
        coursePublishPre.setCompanyId(companyId);
        //审核状态
        coursePublishPre.setStatus("202003");
        //创建时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    @Override
    @Transactional
    public void publishCourse(Long companyId, Long courseId) {
        if(courseId == null) {
            throw new GlobalException("课程id为空");
        }
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            throw new GlobalException("请先提交课程审核，审核通过才可以发布");
        }
        if(!companyId.equals(coursePublishPre.getCompanyId())){
            throw new GlobalException("本机构没有该课程的权限");
        }
        if(!coursePublishPre.getStatus().equals("202004")){
            throw new GlobalException("课程没有审核通过");
        }
        //保存课程发布信息
        saveCoursePublish(coursePublishPre, courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    /**
     * 获取课程发布信息
     * @param courseId
     * @return
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            throw new GlobalException(CommonError.UNKOWN_ERROR.getErrMessage());
        }

    }

    private void saveCoursePublish(CoursePublishPre coursePublishPre, Long courseId) {
        //整合课程发布信息

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //设置发布状态
        coursePublish.setStatus("203002");
        coursePublish.setCreateDate(LocalDateTime.now());
        coursePublish.setOnlineDate(LocalDateTime.now());
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

}
