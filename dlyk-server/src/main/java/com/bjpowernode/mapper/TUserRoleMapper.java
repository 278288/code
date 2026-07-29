package com.bjpowernode.mapper;

import com.bjpowernode.model.TRole;
import com.bjpowernode.model.TUserRole;

import java.util.List;

public interface TUserRoleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TUserRole record);

    int insertSelective(TUserRole record);

    TUserRole selectByPrimaryKey(Integer id);

    /** 查询某用户的所有角色 */
    List<TRole> selectRolesByUserId(Integer userId);

    /** 删除某用户的所有角色 */
    int deleteByUserId(Integer userId);

    /** 批量插入用户角色 */
    int insertBatch(List<com.bjpowernode.model.TUserRole> list);

    int updateByPrimaryKeySelective(TUserRole record);

    int updateByPrimaryKey(TUserRole record);
}
