package com.xia.orders.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class OrderVO {
    private Long orderNo;

    private LocalDateTime createDate;

    private String userId;

    private String coursePubName;

    private BigDecimal price;

    private String status;
}
