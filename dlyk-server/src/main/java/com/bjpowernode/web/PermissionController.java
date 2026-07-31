package com.bjpowernode.web;

import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.model.TPermission;
import com.bjpowernode.result.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理 Controller。
 *
 * 提供"可授权模块列表"接口，供管理员分配权限时使用。
 * 可授权范围只包含 assignable=1 的权限（权限管理模块被标记为不可授权，仅管理员持有）。
 */
@RestController
public class PermissionController {

    @Resource
    private TPermissionMapper tPermissionMapper;

    /**
     * 获取可授权模块列表（排除不可授权模块，如权限管理）。
     * 每个模块包含其按钮子权限，用于权限分配对话框。
     * GET /api/permissions/assignable-modules
     */
    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/api/permissions/assignable-modules")
    public R getAssignableModules() {
        List<TPermission> allPermissions = tPermissionMapper.selectAll();

        // 找出所有可授权的一级菜单模块（排除 assignable=0 的权限管理模块）
        List<TPermission> topModules = allPermissions.stream()
                .filter(p -> p.getType() != null && "menu".equals(p.getType())
                          && (p.getParentId() == null || p.getParentId() == 0)
                          && Integer.valueOf(1).equals(p.getAssignable()))
                .collect(Collectors.toList());

        // 为每个一级菜单找到其可授权的按钮子权限
        for (TPermission topModule : topModules) {
            List<TPermission> buttonChildren = allPermissions.stream()
                    .filter(p -> p.getType() != null && "button".equals(p.getType())
                              && topModule.getId().equals(p.getParentId())
                              && Integer.valueOf(1).equals(p.getAssignable()))
                    .collect(Collectors.toList());

            // 如果没有直接的按钮子权限，找子菜单下的按钮权限
            if (buttonChildren.isEmpty()) {
                List<TPermission> subMenus = allPermissions.stream()
                        .filter(p -> p.getType() != null && "menu".equals(p.getType())
                                  && topModule.getId().equals(p.getParentId())
                                  && Integer.valueOf(1).equals(p.getAssignable()))
                        .collect(Collectors.toList());
                for (TPermission subMenu : subMenus) {
                    List<TPermission> subButtons = allPermissions.stream()
                            .filter(p -> p.getType() != null && "button".equals(p.getType())
                                      && subMenu.getId().equals(p.getParentId())
                                      && Integer.valueOf(1).equals(p.getAssignable()))
                            .collect(Collectors.toList());
                    buttonChildren.addAll(subButtons);
                }
            }

            topModule.setSubPermissionList(buttonChildren);
        }

        return R.OK(topModules);
    }
}
