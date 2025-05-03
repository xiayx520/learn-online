package com.xia.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.base.exception.GlobalException;
import com.xia.content.model.po.CoursePublish;
import com.xia.learning.feignclient.ContentServiceClient;
import com.xia.learning.mapper.XcChooseCourseMapper;
import com.xia.learning.mapper.XcCourseTablesMapper;
import com.xia.learning.model.po.XcChooseCourse;
import com.xia.learning.model.po.XcCourseTables;
import com.xia.learning.model.vo.XcChooseCourseVO;
import com.xia.learning.model.vo.XcCourseTablesVO;
import com.xia.learning.service.XcChooseCourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class XcChooseCourseServiceImpl implements XcChooseCourseService {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;


    /**
     * 添加选课
     *
     * @param courseId
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public XcChooseCourseVO addChooseCourse(Long courseId, String userId) {
        //查询课程发布表
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);
        if (coursePublish == null) {
            log.info("课程不存在");
            return null;
        }
        //根据收费标准添加选课
        XcChooseCourse chooseCourse = null;
        String charge = coursePublish.getCharge();
        if ("201000".equals(charge)) {
            //添加免费课程,
            // 免费课程先添加到选课表，
            //添加免费课程
            chooseCourse = addFreeCoruse(userId, coursePublish);
            //添加到我的课程表
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);

        } else {
            //添加收费课程
            chooseCourse = addChargeCoruse(userId, coursePublish);
        }
        XcChooseCourseVO xcChooseCourseVO = new XcChooseCourseVO();
        BeanUtils.copyProperties(chooseCourse, xcChooseCourseVO);
        //获取学习资格
        XcCourseTablesVO xcCourseTablesVO = getLearningStatus(userId, courseId);
        xcChooseCourseVO.setLearnStatus(xcCourseTablesVO.getLearnStatus());
        return xcChooseCourseVO;
    }

    public XcCourseTablesVO getLearningStatus(String userId, Long courseId) {
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null) {
            XcCourseTablesVO xcCourseTablesVO = new XcCourseTablesVO();
            //没有选课或选课后没有支付
            xcCourseTablesVO.setLearnStatus("702002");
            return xcCourseTablesVO;
        }
        XcCourseTablesVO xcCourseTablesVO = new XcCourseTablesVO();
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesVO);
        //是否过期,true过期，false未过期
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (!isExpires) {
            //正常学习
            xcCourseTablesVO.setLearnStatus("702001");
            return xcCourseTablesVO;

        } else {
            //已过期
            xcCourseTablesVO.setLearnStatus("702003");
            return xcCourseTablesVO;
        }
    }

    /**
     * 添加收费课程
     *
     * @param userId
     * @param coursepublish
     * @return
     */
    private XcChooseCourse addChargeCoruse(String userId, CoursePublish coursepublish) {
        //如果存在待支付记录直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")//收费订单
                .eq(XcChooseCourse::getStatus, "701002");//待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && !xcChooseCourses.isEmpty()) {
            return xcChooseCourses.get(0);
        }

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

    /**
     * 添加课程表
     *
     * @param xcChooseCourse
     * @return
     */
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse) {
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)) {
            throw new GlobalException("选课未成功，无法添加到课程表");
        }

        //查询我的课程表
        XcCourseTables xcCourseTablesDb = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTablesDb != null) {
            return xcCourseTablesDb;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;


    }

    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }

    /**
     * 添加免费课程
     *
     * @param userId
     * @param coursePublish
     * @return
     */
    private XcChooseCourse addFreeCoruse(String userId, CoursePublish coursePublish) {
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001"));//选课成功
        if (xcChooseCourses != null && xcChooseCourses.size() > 0) {
            return xcChooseCourses.get(0);
        }

        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setCoursePrice(BigDecimal.valueOf(0));//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;
    }
}
