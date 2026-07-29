package com.bjpowernode.mapper;

import com.bjpowernode.model.TPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TPermissionMapper {

    /** 查询所有权限（用于构建权限树） */
    List<TPermission> selectAll();

    /** 根据用户ID查询菜单权限（前端侧边栏菜单渲染） */
    List<TPermission> selectMenuPermissionByUserId(Integer userId);

    /** 根据用户ID查询按钮权限（前端按钮显隐控制） */
    List<TPermission> selectButtonPermissionByUserId(Integer userId);

    /** 根据ID查询权限 */
    TPermission selectByPrimaryKey(Integer id);

    /** 根据ID列表批量查询权限 */
    List<TPermission> selectByIds(@Param("list") List<Integer> ids);

    /** 查询可授权模块（排除权限管理本身），每个模块包含其按钮子权限 */
    List<TPermission> selectAssignableModules();

    int deleteByPrimaryKey(Integer id);

    int insert(TPermission record);

    int insertSelective(TPermission record);

    int updateByPrimaryKeySelective(TPermission record);

    int updateByPrimaryKey(TPermission record);
}
