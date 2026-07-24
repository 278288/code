package com.bjpowernode.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TranQuery extends BaseQuery {

    private Integer id;

    private Integer customerId;

    private BigDecimal money;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedDate;

    private Integer stage;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextContactTime;
}