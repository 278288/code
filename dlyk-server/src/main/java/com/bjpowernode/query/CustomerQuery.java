package com.bjpowernode.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class CustomerQuery extends BaseQuery {

    @NotNull(message = "线索ID不能为空")
    private Integer clueId;

    @NotNull(message = "意向产品不能为空")
    private Integer product;

    private String description;

    private Date nextContactTime;
}