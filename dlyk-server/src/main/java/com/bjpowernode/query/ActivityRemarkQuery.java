package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityRemarkQuery extends BaseQuery {

    private Integer id;

    @NotNull(message = "活动ID不能为空")
    private Integer activityId;

    @NotBlank(message = "备注内容不能为空")
    private String noteContent;

}