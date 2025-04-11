package com.xia.content.model.vo;

import com.xia.content.model.po.CourseTeacher;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
public class CoursePreviewVO {

    // 课程基本信息
    CourseBaseInfoVO courseBase;
    // 课程计划信息
    List<TeachPlanVO> teachplans;
    // 课程师资信息
    List<CourseTeacher> courseTeacher;
}
