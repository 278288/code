package com.bjpowernode.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TranQuery extends BaseQuery {

    private Integer id;

    @NotNull(message = "客户不能为空")
    private Integer customerId;

    @NotNull(message = "交易金额不能为空")
    private BigDecimal money;

    @NotNull(message = "预计成交日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedDate;

    @NotNull(message = "交易阶段不能为空")
    private Integer stage;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextContactTime;
}