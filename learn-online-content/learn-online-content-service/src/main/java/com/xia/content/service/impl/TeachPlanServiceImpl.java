package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.base.exception.GlobalException;
import com.xia.content.mapper.TeachplanMapper;
import com.xia.content.mapper.TeachplanMediaMapper;
import com.xia.content.mapper.TeachplanWorkMapper;
import com.xia.content.model.dto.BindTeachplanMediaDto;
import com.xia.content.model.dto.SaveTeachplanDto;
import com.xia.content.model.po.Teachplan;
import com.xia.content.model.po.TeachplanMedia;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.model.vo.TeachplanWorkVO;
import com.xia.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    private TeachplanWorkMapper teachplanWorkMapper;

    /**
     * 获取课程计划
     *
     * @param courseId 课程id
     * @return 课程计划
     */
    @Override
    public List<TeachPlanVO> getTeachPlanTree(Long courseId) {
        return teachplanMapper.getTeachPlanTree(courseId);
    }

    /**
     * 获取课程计划树形结构，包含作业信息
     *
     * @param courseId 课程id
     * @return 课程计划树形结构(包含作业信息)
     */
    @Override
    public List<TeachPlanVO> getTeachPlanTreeWithWork(Long courseId) {
        // 获取课程计划基本结构
        List<TeachPlanVO> teachPlanTree = getTeachPlanTree(courseId);
        
        // 获取课程关联的所有作业信息
        List<TeachplanWorkVO> teachplanWorks = teachplanWorkMapper.getTeachplanWorksByCourseId(courseId);
        
        // 将作业信息关联到对应的课程计划中
        if (teachPlanTree != null && !teachPlanTree.isEmpty() && teachplanWorks != null && !teachplanWorks.isEmpty()) {
            // 为每个课程计划节点关联作业信息
            associateWorkWithTeachplan(teachPlanTree, teachplanWorks);
        }
        
        return teachPlanTree;
    }
    
    /**
     * 递归关联作业与课程计划
     * @param teachPlanNodes 课程计划节点
     * @param teachplanWorks 作业信息列表
     */
    private void associateWorkWithTeachplan(List<TeachPlanVO> teachPlanNodes, List<TeachplanWorkVO> teachplanWorks) {
        if (teachPlanNodes == null || teachplanWorks == null) {
            return;
        }
        
        for (TeachPlanVO teachPlanNode : teachPlanNodes) {
            // 只有小节(grade=2)才能关联作业
            if (teachPlanNode.getGrade() == 2) {
                // 查找对应的作业信息
                for (TeachplanWorkVO teachplanWork : teachplanWorks) {
                    if (teachPlanNode.getId().equals(teachplanWork.getTeachplanId())) {
                        teachPlanNode.setTeachplanWork(teachplanWork);
                        break;
                    }
                }
            }
            
            // 递归处理子节点
            List<TeachPlanVO> childNodes = teachPlanNode.getTeachPlanTreeNodes();
            if (childNodes != null && !childNodes.isEmpty()) {
                associateWorkWithTeachplan(childNodes, teachplanWorks);
            }
        }
    }

    /**
     * 保存课程计划
     *
     * @param saveTeachplanDto
     */
    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        //课程计划id
        Long id = saveTeachplanDto.getId();
        //修改课程计划
        if (id != null) {
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        } else {
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count + 1);
            BeanUtils.copyProperties(saveTeachplanDto, teachplanNew);

            teachplanMapper.insert(teachplanNew);
        }
    }

    /**
     * 删除课程计划
     *
     * @param id
     */
    @Override
    @Transactional
    public void deleteTeachPlan(Long id) {
        //判断id对应的课程计划是否存在
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            throw new GlobalException("课程计划不存在");
        }
        //判断课程计划是否为父级课程计划,如果是父级课程计划则要判断改父级课程下是否有子级计划，如果有则不允许删除
        if (teachplan.getParentid() == 0) {
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, id);
            queryWrapper.eq(Teachplan::getStatus, 1);
            int count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new GlobalException("该课程计划下有子级课程计划，不允许删除");
            }
        }
        //删除小章节，同时将关联的信息进行删除。
        teachplan.setStatus(0);
        teachplanMapper.updateById(teachplan);
        //删除课程计划媒资信息
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplan.getId()));
    }

    /**
     * 移动课程计划
     *
     * @param moveType
     * @param id
     */
    @Override
    @Transactional
    public void moveTeachPlan(String moveType, Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            throw new GlobalException("课程计划不存在");
        }
        //查询该课程下同父节点的全部的课程计划，升序排序
        List<Teachplan> teachplans = teachplanMapper.selectList(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .eq(Teachplan::getStatus, 1)
                .orderByAsc(Teachplan::getOrderby));
        //判断移动类型
        if (moveType.equals("moveup")) {
            for (int i = 0; i < teachplans.size(); i++) {
                Teachplan teachplan1 = teachplans.get(i);
                if (teachplan1.getId().equals(id)) {
                    if (i == 0) {
                        throw new GlobalException("该课程计划已经是同级课程计划中的第一个");
                    }
                    Teachplan teachplan2 = teachplans.get(i - 1);
                    int orderby = teachplan1.getOrderby();
                    teachplan1.setOrderby(teachplan2.getOrderby());
                    teachplan2.setOrderby(orderby);
                    teachplanMapper.updateById(teachplan1);
                    teachplanMapper.updateById(teachplan2);
                }
            }
        } else {
            for (int i = 0; i < teachplans.size(); i++) {
                Teachplan teachplan1 = teachplans.get(i);
                if (teachplan1.getId().equals(id)) {
                    if (i == teachplans.size() - 1) {
                        throw new GlobalException("该课程计划已经是同级课程计划中的最后一个");
                    }
                    Teachplan teachplan2 = teachplans.get(i + 1);
                    int orderby = teachplan1.getOrderby();
                    teachplan1.setOrderby(teachplan2.getOrderby());
                    teachplan2.setOrderby(orderby);
                    teachplanMapper.updateById(teachplan1);
                    teachplanMapper.updateById(teachplan2);
                }
            }
        }
    }

    /**
     * 绑定媒资信息
     *
     * @param bindTeachplanMediaDto
     */
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //判断字段是否为空
        if (bindTeachplanMediaDto == null) {
            throw new GlobalException("绑定媒资信息为空");
        }

        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            throw new GlobalException("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            throw new GlobalException("只允许第二级教学计划绑定媒资文件");
        }

        //先删除，再插入
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachplanId));

        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);

    }

    @Override
    public void deleteTeachPlanMedia(Long teachPlanId, String mediaId) {
        //判断媒资文件id是否为空
        if (mediaId == null) {
            throw new GlobalException("媒资文件id为空");
        }
        //判断教学计划id是否为空
        if (teachPlanId == null) {
            throw new GlobalException("教学计划id为空");
        }

        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachPlanId)
                .eq(TeachplanMedia::getMediaId, mediaId));
    }

    /**
     * 判断是否为预览
     * @param teachPlanId
     * @return
     */
    @Override
    public Boolean isPreview(Long teachPlanId) {
        if (teachPlanId == null) {
            throw new GlobalException("教学计划id为空");
        }
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null) {
            throw new GlobalException("教学计划不存在");
        }
        String isPreview = teachplan.getIsPreview();

        return "1".equals(isPreview);
    }

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }


}
