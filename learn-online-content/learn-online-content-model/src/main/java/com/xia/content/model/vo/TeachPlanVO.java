package com.xia.content.model.vo;

import com.xia.content.model.po.Teachplan;
import com.xia.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachPlanVO extends Teachplan {
    //课程计划媒资信息
    private TeachplanMedia teachplanMedia;
    //子节点
    private List<TeachPlanVO> teachPlanTreeNodes;
}
