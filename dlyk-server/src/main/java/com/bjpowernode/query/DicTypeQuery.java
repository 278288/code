package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DicTypeQuery extends BaseQuery {

    private Integer id;

    @NotBlank(message = "字典类型代码不能为空")
    private String typeCode;

    @NotBlank(message = "字典类型名称不能为空")
    private String typeName;

    private String remark;
}