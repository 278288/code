package com.bjpowernode.model;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户个人权限关联表，对应 t_user_permission。
 * 补充角色权限以外的单独授权。
 */
@Data
public class TUserPermission implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer permissionId;
    private static final long serialVersionUID = 1L;
}