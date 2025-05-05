package com.xia.orders.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单查询条件DTO
 */
@Data
public class OrderQueryDto {
    /**
     * 订单开始时间（格式示例：2023-08-01 00:00:00）
     */
    private LocalDateTime orderStart;

    /**
     * 订单结束时间（格式示例：2023-08-31 23:59:59）
     */
    private LocalDateTime orderEnd;

    /**
     * 课程名称（模糊匹配）
     */
    private String courseName;

    /**
     * 订单状态[{"code":"600001","desc":"未支付"},{"code":"600002","desc":"已支付"},{"code":"600003","desc":"已关闭"},{"code":"600004","desc":"已退款"},{"code":"600005","desc":"已完成"}]
     */
    private String status;

    /**
     * 订单编号（精确匹配）
     */
    private String orderNo;

    /**
     * 用户ID（精确匹配）
     */
    private String userId;
}

