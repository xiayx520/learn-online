package com.xia.learning.service.impl;

import com.xia.learning.mapper.XcChooseCourseMapper;
import com.xia.learning.model.po.XcChooseCourse;
import com.xia.learning.service.MyCourseTablesService;
import com.xia.learning.service.XcChooseCourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    private XcChooseCourseMapper chooseCourseMapper;

    @Autowired
    private XcChooseCourseService xcChooseCourseService;


    /**
     * 保存选课记录到我的课表
     *
     * @param chooseCourseId
     * @return
     */
    @Override
    @Transactional
    public boolean saveChooseCourseSuccess(String chooseCourseId) {
        //根据choosecourseId查询选课记录
        XcChooseCourse xcChooseCourse = chooseCourseMapper.selectById(chooseCourseId);
        if (xcChooseCourse == null) {
            log.debug("收到支付结果通知没有查询到关联的选课记录,choosecourseId:{}", chooseCourseId);
            return false;
        }
        String status = xcChooseCourse.getStatus();
        if ("701001".equals(status)) {
            //添加到课程表
            xcChooseCourseService.addCourseTabls(xcChooseCourse);
            return true;
        }
        //待支付状态才处理
        if ("701002".equals(status)) {
            //更新为选课成功
            xcChooseCourse.setStatus("701001");
            int update = chooseCourseMapper.updateById(xcChooseCourse);
            if (update > 0) {
                log.debug("收到支付结果通知处理成功,选课记录:{}", xcChooseCourse);
                //添加到课程表
                xcChooseCourseService.addCourseTabls(xcChooseCourse);
                return true;
            } else {
                log.debug("收到支付结果通知处理失败,选课记录:{}", xcChooseCourse);
                return false;
            }
        }
        return false;
    }

}
