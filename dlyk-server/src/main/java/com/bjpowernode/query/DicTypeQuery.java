package com.bjpowernode.query;

import lombok.Data;

@Data
public class DicTypeQuery extends BaseQuery {

    private Integer id;

    private String typeCode;

    private String typeName;

    private String remark;
}