package com.xia.content.service.impl;

import com.xia.content.mapper.CourseCategoryMapper;
import com.xia.content.model.vo.CourseCategoryTreeVO;
import com.xia.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    /**
     * 查询课程分类
     * @param parentId 课程父级id
     * @return 课程分类
     */
    @Override
    public List<CourseCategoryTreeVO> queryTreeNodes(String parentId) {
        // 查询该父级分类下的所有分类
        List<CourseCategoryTreeVO> courseCategory = courseCategoryMapper.queryTreeNodes(parentId);

        List<CourseCategoryTreeVO> courseCategoryTreeVOS = new ArrayList<>();
        //依次遍历每个元素,排除根节点
        courseCategory.stream().filter(item -> !parentId.equals(item.getId())).forEach(
                item -> {
                    if(item.getParentid().equals(parentId)){
                        courseCategoryTreeVOS.add(item);
                    }
                    //如果该节点的父节点id在courseCategory里,则添加到对应的父节点下
                    for(CourseCategoryTreeVO courseCategoryTreeVO : courseCategory){
                        if(courseCategoryTreeVO.getId().equals(item.getParentid())){
                            if(courseCategoryTreeVO.getChildrenTreeNodes() == null){
                                courseCategoryTreeVO.setChildrenTreeNodes(new ArrayList<>());
                            }
                            courseCategoryTreeVO.getChildrenTreeNodes().add(item);
                        }
                    }

                }
        );


        return courseCategoryTreeVOS;
    }
}
