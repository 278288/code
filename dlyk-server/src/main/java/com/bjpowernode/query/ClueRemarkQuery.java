package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClueRemarkQuery extends BaseQuery {

    @NotNull(message = "线索ID不能为空")
    private Integer clueId;

    @NotBlank(message = "备注内容不能为空")
    private String noteContent;

    private Integer noteWay;
}