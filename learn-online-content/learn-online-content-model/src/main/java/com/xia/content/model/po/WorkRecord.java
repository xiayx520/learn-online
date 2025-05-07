package com.xia.content.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业记录实体类
 */
@Data
@TableName("work_record")
public class WorkRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID
     */
    private Long workId;

    /**
     * 学生ID
     */
    private Long userId;

    /**
     * 作业状态：pending-待完成，submitted-已提交，graded-已评分
     */
    private String status;

    /**
     * 提交内容
     */
    private String submitContent;

    /**
     * 提交时间
     */
    private LocalDateTime submitDate;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 修改时间
     */
    private LocalDateTime changeDate;
}