package com.bjpowernode.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserQuery extends BaseQuery {

    /**
     * 主键，自动增长，用户ID
     */
    private Integer id;

    /**
     * 登录账号
     */
    @NotBlank(message = "登录账号不能为空")
    private String loginAct;

    /**
     * 登录密码（创建用户时必填，编辑时可不填）
     */
    private String loginPwd;

    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空")
    private String name;

    /**
     * 用户手机
     */
    private String phone;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 账户是否没有过期，0已过期 1正常
     */
    private Integer accountNoExpired;

    /**
     * 密码是否没有过期，0已过期 1正常
     */
    private Integer credentialsNoExpired;

    /**
     * 账号是否没有锁定，0已锁定 1正常
     */
    private Integer accountNoLocked;

    /**
     * 账号是否启用，0禁用 1启用
     */
    private Integer accountEnabled;

}