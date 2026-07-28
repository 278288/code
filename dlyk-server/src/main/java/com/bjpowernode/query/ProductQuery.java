package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductQuery extends BaseQuery {

    private Integer id;

    @NotBlank(message = "产品名称不能为空")
    private String name;

    @NotNull(message = "指导价不能为空")
    private BigDecimal guidePriceS;

    private BigDecimal guidePriceE;

    @NotNull(message = "报价不能为空")
    private BigDecimal quotation;

    @NotNull(message = "状态不能为空")
    private Integer state;
}