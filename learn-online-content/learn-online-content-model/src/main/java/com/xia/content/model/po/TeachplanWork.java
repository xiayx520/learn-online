package com.xia.content.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teachplan_work")
public class TeachplanWork {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long workId;
    private String workTitle;
    private Long teachplanId;
    private Long courseId;
    private Long coursePubId;
    private LocalDateTime createDate;
} 