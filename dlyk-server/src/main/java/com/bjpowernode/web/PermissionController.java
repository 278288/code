package com.bjpowernode.web;

import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.model.TPermission;
import com.bjpowernode.result.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理 Controller
 * 用于获取权限树形结构、可授权模块列表等操作
 */
@RestController
public class PermissionController {

    @Resource
    private TPermissionMapper tPermissionMapper;

    /**
     * 获取权限树形结构
     * GET /api/permissions/tree
     */
    @PreAuthorize("hasAuthority('perm:list')")
    @GetMapping("/api/permissions/tree")
    public R getPermissionTree() {
        List<TPermission> allPermissions = tPermissionMapper.selectAll();
        List<TPermission> tree = buildPermissionTree(allPermissions);
        return R.OK(tree);
    }

    /**
     * 获取可授权模块列表（排除权限管理本身）
     * 每个模块包含其按钮子权限，用于权限分配对话框
     * GET /api/permissions/assignable-modules
     */
    @PreAuthorize("hasAuthority('user:edit')")
    @GetMapping("/api/permissions/assignable-modules")
    public R getAssignableModules() {
        List<TPermission> allPermissions = tPermissionMapper.selectAll();
        
        // 找出所有一级菜单模块（排除权限管理 id=67）
        List<TPermission> topModules = allPermissions.stream()
                .filter(p -> p.getType() != null && "menu".equals(p.getType()) 
                          && (p.getParentId() == null || p.getParentId() == 0)
                          && p.getId() != 67)
                .collect(Collectors.toList());
        
        // 为每个一级菜单找到其按钮子权限
        for (TPermission topModule : topModules) {
            List<TPermission> buttonChildren = allPermissions.stream()
                    .filter(p -> p.getType() != null && "button".equals(p.getType())
                              && topModule.getId().equals(p.getParentId()))
                    .collect(Collectors.toList());
            
            // 如果没有直接的按钮子权限，找子菜单下的按钮权限
            if (buttonChildren.isEmpty()) {
                List<TPermission> subMenus = allPermissions.stream()
                        .filter(p -> p.getType() != null && "menu".equals(p.getType())
                                  && topModule.getId().equals(p.getParentId()))
                        .collect(Collectors.toList());
                for (TPermission subMenu : subMenus) {
                    List<TPermission> subButtons = allPermissions.stream()
                            .filter(p -> p.getType() != null && "button".equals(p.getType())
                                      && subMenu.getId().equals(p.getParentId()))
                            .collect(Collectors.toList());
                    buttonChildren.addAll(subButtons);
                }
            }
            
            topModule.setSubPermissionList(buttonChildren);
        }
        
        return R.OK(topModules);
    }

    /**
     * 构建权限树形结构
     */
    private List<TPermission> buildPermissionTree(List<TPermission> permissions) {
        List<TPermission> rootPermissions = permissions.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());

        for (TPermission permission : rootPermissions) {
            buildChildren(permission, permissions);
        }

        return rootPermissions;
    }

    /**
     * 递归构建权限子树
     */
    private void buildChildren(TPermission parent, List<TPermission> allPermissions) {
        List<TPermission> children = allPermissions.stream()
                .filter(p -> parent.getId().equals(p.getParentId()))
                .collect(Collectors.toList());

        parent.setSubPermissionList(children);

        for (TPermission child : children) {
            buildChildren(child, allPermissions);
        }
    }
}
