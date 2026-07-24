package com.bjpowernode.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class TTran implements Serializable {
    private Integer id;
    private String tranNo;
    private Integer customerId;
    private BigDecimal money;
    private Date expectedDate;
    private Integer stage;
    private String description;
    private Date nextContactTime;
    private Date createTime;
    private Integer createBy;
    private Date editTime;
    private Integer editBy;

    private TCustomer customerDO = new TCustomer();

    private static final long serialVersionUID = 1L;
}