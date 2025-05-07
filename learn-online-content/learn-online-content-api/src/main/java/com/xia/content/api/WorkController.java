package com.xia.content.api;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.model.dto.*;
import com.xia.content.model.po.TeachplanWork;
import com.xia.content.model.po.WorkInfo;
import com.xia.content.model.po.WorkRecord;
import com.xia.content.service.TeachplanWorkService;
import com.xia.content.service.WorkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "作业管理接口", tags = "作业管理接口")
@RestController
@Slf4j
public class WorkController {

    @Autowired
    private WorkService workService;

    @Autowired
    private TeachplanWorkService teachplanWorkService;

    @ApiOperation("创建/更新作业")
    @PostMapping("/work/save")
    public WorkInfo saveWork(@RequestBody WorkInfoDto workInfoDto) {
        log.info("创建/更新作业，参数：{}", workInfoDto);
        return workService.saveWork(workInfoDto);
    }

    @ApiOperation("发布作业")
    @PostMapping("/work/publish/{workId}")
    public void publishWork(@PathVariable("workId") Long workId) {
        log.info("发布作业，参数：{}", workId);
        workService.publishWork(workId);
    }

    @ApiOperation("归档作业")
    @PostMapping("/work/archive/{workId}")
    public void archiveWork(@PathVariable("workId") Long workId) {
        log.info("归档作业，参数：{}", workId);
        workService.archiveWork(workId);
    }

    @ApiOperation("查询作业列表")
    @PostMapping("/work/list")
    public PageResult<WorkInfo> listWorks(PageParams pageParams, @RequestBody QueryWorkParamsDto queryWorkParamsDto) {
        log.info("查询作业列表，参数：{}", queryWorkParamsDto);
        return workService.queryWorkList(pageParams, queryWorkParamsDto.getStatus());
    }


    @ApiOperation("提交作业")
    @PostMapping("/work/submit")
    public WorkRecord submitWork(@RequestBody WorkSubmitDTO workSubmitDTO) {
        log.info("提交作业，参数：{}", workSubmitDTO);
        return workService.submitWork(workSubmitDTO);
    }

    @ApiOperation("查询作业提交记录")
    @PostMapping("/work/record/list")
    public PageResult<WorkRecord> listWorkRecords(PageParams pageParams, @RequestBody QueryWorkRecordParamsDto queryWorkRecordParamsDto) {
        log.info("查询作业提交记录，参数：{}", queryWorkRecordParamsDto);
        return workService.queryWorkRecordList(pageParams, queryWorkRecordParamsDto);
    }


    @PostMapping("/work/grade")
    @ApiOperation("评分")
    public void grade(@RequestBody @Validated WorkGradeDTO workGradeDTO) {
        log.info("评分，参数：{}", workGradeDTO);
        workService.grade(workGradeDTO);
    }

    @ApiOperation("删除作业")
    @DeleteMapping("/work/delete/{workId}")
    public void deleteWork(@PathVariable("workId") Long workId) {
        log.info("删除作业，参数：{}", workId);
        workService.deleteWork(workId);
    }

    @ApiOperation("下线作业")
    @PostMapping("/work/unpublish/{workId}")
    public void unpublishWork(@PathVariable("workId") Long workId) {
        log.info("下线作业，参数：{}", workId);
        workService.unpublishWork(workId);
    }

    @ApiOperation("取消归档")
    @PostMapping("/work/unarchive/{workId}")
    public void unarchiveWork(@PathVariable("workId") Long workId) {
        log.info("取消归档，参数：{}", workId);
        workService.unarchiveWork(workId);
    }

    @ApiOperation("绑定课程计划")
    @PostMapping("/work/bind-teachplan/{workId}")
    public void bindTeachplan(@PathVariable("workId") Long workId, @RequestBody List<Long> teachplanIds) {
        log.info("绑定课程计划，作业ID：{}，课程计划IDs：{}", workId, teachplanIds);
        teachplanWorkService.bindWorkToTeachplan(workId, teachplanIds);
    }

    @ApiOperation("解绑课程计划")
    @DeleteMapping("/work/unbind-teachplan/{workId}/{teachplanId}")
    public void unbindTeachplan(@PathVariable("workId") Long workId, @PathVariable("teachplanId") Long teachplanId) {
        log.info("解绑课程计划，作业ID：{}，课程计划ID：{}", workId, teachplanId);
        teachplanWorkService.unbindWorkFromTeachplan(workId, teachplanId);
    }

    @ApiOperation("获取作业绑定的课程计划")
    @GetMapping("/work/teachplans/{workId}")
    public List<TeachplanWork> getTeachplans(@PathVariable("workId") Long workId) {
        log.info("获取作业绑定的课程计划，作业ID：{}", workId);
        return teachplanWorkService.getTeachplansByWorkId(workId);
    }

    @ApiOperation("获取作业批改详情")
    @GetMapping("/work-record/read-over-all/{workId}")
    public IWorkRecOverallDTO getWorkRecordReadOverAll(@PathVariable("workId") Long workId) {
        log.info("获取作业批改详情，作业ID：{}", workId);
        return workService.getWorkRecordReadOverAll(workId);
    }

    @ApiOperation("批改作业")
    @PutMapping("/work-record/correction")
    public void correctWorkRecord(@RequestBody WorkGradeDTO workGradeDTO) {
        log.info("批改作业，参数：{}", workGradeDTO);
        workService.grade(workGradeDTO);
    }
}