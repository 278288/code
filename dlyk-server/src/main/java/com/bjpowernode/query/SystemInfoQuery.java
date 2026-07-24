package com.bjpowernode.query;

import lombok.Data;

@Data
public class SystemInfoQuery extends BaseQuery {

    private Integer id;

    private String systemCode;

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