package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xia.base.exception.GlobalException;
import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.mapper.*;
import com.xia.content.model.dto.AddCourseDto;
import com.xia.content.model.dto.EditCourseDto;
import com.xia.content.model.dto.QueryCourseParamsDto;
import com.xia.content.model.po.*;
import com.xia.content.model.vo.CourseBaseInfoVO;
import com.xia.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private TeachplanMapper teachPlanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     * 根据查询条件分页查询课程
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //使用lambda表达式构建QueryWrapper
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNoneEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNoneEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNoneEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //构造分页查询条件
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        return new PageResult<CourseBase>(courseBasePage.getRecords(),courseBasePage.getTotal(),pageParams.getPageNo(),pageParams.getPageSize());
    }


    /**
     * 新增课程
     * @param addCourseDto
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoVO createCourseBase(AddCourseDto addCourseDto) {
        //合法性校验
//        if (StringUtils.isBlank(addCourseDto.getName())) {
//            throw new GlobalException("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getMt())) {
//            throw new GlobalException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getSt())) {
//            throw new GlobalException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getGrade())) {
//            throw new GlobalException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
//            throw new GlobalException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getUsers())) {
//            throw new GlobalException("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            throw new GlobalException("收费规则为空");
//        }
        //封装程基本信息数据
        CourseBase courseBaseNew = new CourseBase();
        //拷贝添加课程基本信息
        BeanUtils.copyProperties(addCourseDto,courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //TODO: 机构id
        courseBaseNew.setCompanyId(1232141425L);

        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        int insertCourseBase = courseBaseMapper.insert(courseBaseNew);
        if(insertCourseBase < 0){
            throw new GlobalException("添加课程基本信息失败");
        }

        //封装程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto,courseMarketNew);
        courseMarketNew.setId(courseBaseNew.getId());
        //保存营销信息
        saveCourseMarket(courseMarketNew);

        return getCourseBaseInfo(courseBaseNew.getId());
    }

    public void saveCourseMarket(CourseMarket courseMarketNew) {
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new GlobalException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null
                    || courseMarketNew.getPrice().floatValue()<=0
                    || courseMarketNew.getOriginalPrice() == null
                    || courseMarketNew.getOriginalPrice().floatValue()<=0){
                throw new GlobalException("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketDB = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketDB == null){
            int insertCourseMarket = courseMarketMapper.insert(courseMarketNew);
            if(insertCourseMarket < 0){
                throw new GlobalException("添加课程营销信息失败");
            }
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarketDB);
            int insertCourseMarket = courseMarketMapper.updateById(courseMarketDB);
            if(insertCourseMarket < 0){
                throw new GlobalException("更新课程营销信息失败");
            }
        }
    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoVO getCourseBaseInfo(Long courseId){

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoVO courseBaseInfoVO = new CourseBaseInfoVO();
        BeanUtils.copyProperties(courseBase,courseBaseInfoVO);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoVO);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoVO.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoVO.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoVO;

    }

    /**
     * 修改课程基本信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    @Override
    @Transactional
    public CourseBaseInfoVO updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        //课程id
        Long courseId = editCourseDto.getId();
        //查询课程是否存在
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            throw new GlobalException("课程不存在");
        }
        //判断本机构是否拥有该课程
        if(!companyId.equals(courseBase.getCompanyId())){
            throw new GlobalException("本机构没有该课程的权限");
        }
        //封装课程基本信息
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(editCourseDto,courseBaseNew);
        courseBaseNew.setChangeDate(LocalDateTime.now());
        int update = courseBaseMapper.updateById(courseBaseNew);

        //封装课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarketNew);
        courseMarketNew.setId(courseId);
        saveCourseMarket(courseMarketNew);

        return getCourseBaseInfo(courseId);
    }

    /**
     * 删除课程基本信息
     * @param courseId
     * @param companyId
     */
    @Override
    @Transactional
    public void deleteCourseBase(Long courseId, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            throw new GlobalException("课程不存在");
        }
        if(!companyId.equals(courseBase.getCompanyId())){
            throw new GlobalException("本机构没有该课程的权限");
        }
        //课程的审核状态为未提交时方可删除
        if(!courseBase.getAuditStatus().equals("202002")){
            throw new GlobalException("课程的审核状态为未提交时方可删除");
        }
        //删除课程基本信息
        int deleteCourseBase = courseBaseMapper.deleteById(courseId);
        if(deleteCourseBase < 0){
            throw new GlobalException("删除课程基本信息失败");
        }
        //删除课程营销信息
        int deleteCourseMarket = courseMarketMapper.deleteById(courseId);
        if(deleteCourseMarket < 0){
            throw new GlobalException("删除课程营销信息失败");
        }
        //删除课程计划信息
        int deleteTeachPlan = teachPlanMapper.delete(
                new LambdaQueryWrapper<Teachplan>()
                        .eq(Teachplan::getCourseId, courseId)
        );
        if(deleteTeachPlan < 0){
            throw new GlobalException("删除课程计划信息失败");
        }
        //删除课程媒资信息
        int deleteCoursePublish = teachplanMediaMapper.delete(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getCourseId, courseId)
        );
        if(deleteCoursePublish < 0){
            throw new GlobalException("删除课程媒资信息失败");
        }
        //删除课程教师信息
        int deleteCourseTeacher = courseTeacherMapper.delete(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
        );

        if(deleteCourseTeacher < 0){
            throw new GlobalException("删除课程教师信息失败");
        }
    }
}
