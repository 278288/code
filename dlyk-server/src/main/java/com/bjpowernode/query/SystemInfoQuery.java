package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemInfoQuery extends BaseQuery {

    private Integer id;

    @NotBlank(message = "系统代码不能为空")
    private String systemCode;

    @NotBlank(message = "系统名称不能为空")
    private String name;

    private String site;

    private String logo;

    private String title;

    private String description;

    private String keywords;

    private String shortcuticon;

    private String tel;

    private String weixin;

    private String email;

    private String address;

    private String version;

    private String closemsg;

    private String isopen;
}