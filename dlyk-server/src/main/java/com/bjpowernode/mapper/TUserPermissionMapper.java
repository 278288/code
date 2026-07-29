package com.bjpowernode.mapper;

import com.bjpowernode.model.TUserPermission;
import java.util.List;

/**
 * 用户个人权限 Mapper。
 */
public interface TUserPermissionMapper {

    /** 查询某用户的所有个人权限 ID 列表 */
    List<Integer> selectPermissionIdsByUserId(Integer userId);

    /** 批量插入用户权限 */
    int insertBatch(List<TUserPermission> list);

    /** 删除某用户的所有个人权限 */
    int deleteByUserId(Integer userId);
}