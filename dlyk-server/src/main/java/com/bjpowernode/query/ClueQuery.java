package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ClueQuery extends BaseQuery {

    private Integer id;

    @NotNull(message = "负责人不能为空")
    private Integer ownerId;

    private Integer activityId;

    @NotBlank(message = "姓名不能为空")
    private String fullName;

    @NotNull(message = "称呼不能为空")
    private Integer appellation;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String weixin;

    private String qq;

    private String email;

    private Integer age;

    private String job;

    private BigDecimal yearIncome;

    private String address;

    private Integer needLoan;

    @NotNull(message = "意向状态不能为空")
    private Integer intentionState;

    private Integer intentionProduct;

    private Integer state;

    @NotNull(message = "线索来源不能为空")
    private Integer source;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextContactTime;
}