package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DicValueQuery extends BaseQuery {

    private Integer id;

    @NotBlank(message = "字典类型代码不能为空")
    private String typeCode;

    @NotBlank(message = "字典值不能为空")
    private String typeValue;

    @NotNull(message = "排序不能为空")
    private Integer order;

    private String remark;
}