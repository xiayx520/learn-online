package com.xia.learning.api;

import com.xia.base.model.PageResult;
import com.xia.base.model.RestResponse;
import com.xia.learning.model.dto.LearnRecordDTO;
import com.xia.learning.model.dto.LearnRecordQueryDTO;
import com.xia.learning.model.vo.LearnRecordVO;
import com.xia.learning.model.vo.LearnStatisticsVO;
import com.xia.learning.model.vo.LearnRankItemVO;
import com.xia.learning.service.LearnRecordService;
import com.xia.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习记录接口
 */
@Slf4j
@Api(value = "学习记录接口", tags = "学习记录接口")
@RestController
@RequestMapping("/learnrecord")
public class LearnRecordController {

    @Autowired
    private LearnRecordService learnRecordService;

    @ApiOperation("保存学习记录")
    @PostMapping("/save")
    public RestResponse<Long> saveLearnRecord(@RequestBody LearnRecordDTO learnRecordDTO) {
        // 获取当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            return RestResponse.validfail("用户未登录");
        }
        
        String userId = user.getId();
        Long recordId = learnRecordService.saveLearnRecord(userId, learnRecordDTO);
        return RestResponse.success(recordId);
    }

    @ApiOperation("查询学习记录")
    @GetMapping("/list")
    public RestResponse<PageResult<LearnRecordVO>> queryLearnRecords(LearnRecordQueryDTO queryDTO) {
        // 获取当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            return RestResponse.validfail("用户未登录");
        }
        
        // 如果没有指定用户ID，使用当前登录用户
        if (queryDTO.getUserId() == null || queryDTO.getUserId().isEmpty()) {
            queryDTO.setUserId(user.getId());
        }
        
        PageResult<LearnRecordVO> pageResult = learnRecordService.queryLearnRecords(queryDTO);
        return RestResponse.success(pageResult);
    }

    @ApiOperation("获取课程学习统计")
    @GetMapping("/statistics/{courseId}")
    public RestResponse<LearnStatisticsVO> getLearnStatistics(@PathVariable("courseId") Long courseId) {
        // 获取当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            return RestResponse.validfail("用户未登录");
        }
        
        LearnStatisticsVO statistics = learnRecordService.getLearnStatistics(user.getId(), courseId);
        return RestResponse.success(statistics);
    }

    @ApiOperation("获取用户总学习时长")
    @GetMapping("/totaltime")
    public RestResponse<Long> getTotalLearnLength() {
        // 获取当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            return RestResponse.validfail("用户未登录");
        }
        
        Long totalLearnLength = learnRecordService.getTotalLearnLength(user.getId());
        return RestResponse.success(totalLearnLength);
    }
    
    @ApiOperation("管理员查询学习记录")
    @GetMapping("/admin/list")
    public RestResponse<PageResult<LearnRecordVO>> adminQueryLearnRecords(LearnRecordQueryDTO queryDTO) {
        // 管理员权限检查逻辑可以在这里添加
        
        PageResult<LearnRecordVO> pageResult = learnRecordService.queryLearnRecords(queryDTO);
        return RestResponse.success(pageResult);
    }
    
    @ApiOperation("管理员获取学习统计")
    @GetMapping("/admin/statistics/{courseId}")
    public RestResponse<LearnStatisticsVO> adminGetLearnStatistics(
            @PathVariable("courseId") Long courseId,
            @RequestParam(required = true) String userId) {
        
        // 管理员权限检查逻辑可以在这里添加
        
        LearnStatisticsVO statistics = learnRecordService.getLearnStatistics(userId, courseId);
        return RestResponse.success(statistics);
    }
    
    @ApiOperation("获取学习时长排行榜")
    @GetMapping("/admin/ranking/learnlength")
    public RestResponse<List<LearnRankItemVO>> getLearnLengthRanking(
            @RequestParam(required = false) Long courseId) {
        
        // 管理员权限检查逻辑可以在这里添加
        
        List<LearnRankItemVO> ranking = learnRecordService.getLearnLengthRanking(courseId);
        return RestResponse.success(ranking);
    }
    
    @ApiOperation("获取完成率排行榜")
    @GetMapping("/admin/ranking/completion")
    public RestResponse<List<LearnRankItemVO>> getCompletionRateRanking(
            @RequestParam(required = false) Long courseId) {
        
        // 管理员权限检查逻辑可以在这里添加
        
        List<LearnRankItemVO> ranking = learnRecordService.getCompletionRateRanking(courseId);
        return RestResponse.success(ranking);
    }
} 