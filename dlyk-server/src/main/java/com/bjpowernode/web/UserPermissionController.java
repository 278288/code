package com.bjpowernode.web;

import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.mapper.TUserPermissionMapper;
import com.bjpowernode.model.TPermission;
import com.bjpowernode.model.TUserPermission;
import com.bjpowernode.result.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户权限管理 Controller。仅管理员（admin 角色）可用。
 * 管理员为用户单独授权模块权限（补充角色权限）。
 *
 * GET  /api/user/{id}/permissions  查询用户当前个人权限 ID 列表（只返回可授权权限）
 * PUT  /api/user/{id}/permissions  保存用户个人权限（先删后插，body: [1,2,3,...]）
 */
@RestController
public class UserPermissionController {

    @Resource
    private TUserPermissionMapper tUserPermissionMapper;

    @Resource
    private TPermissionMapper tPermissionMapper;

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/api/user/{id}/permissions")
    public R getPermissions(@PathVariable Integer id) {
        List<Integer> permIds = tUserPermissionMapper.selectPermissionIdsByUserId(id);
        if (permIds == null || permIds.isEmpty()) {
            return R.OK(permIds);
        }
        // 只返回可授权权限，避免把不可授权模块（权限管理）的历史数据带回前端
        List<TPermission> perms = tPermissionMapper.selectByIds(permIds);
        List<Integer> assignableIds = perms.stream()
                .filter(p -> Integer.valueOf(1).equals(p.getAssignable()))
                .map(TPermission::getId)
                .collect(Collectors.toList());
        return R.OK(assignableIds);
    }

    /**
     * 保存用户个人权限。
     * 校验规则：
     *   1. 所有提交的权限 ID 必须存在；
     *   2. 所有提交的权限必须可授权（assignable=1），否则整个请求拒绝；
     *   3. 保存时自动补齐菜单链（按钮 -> 子菜单 -> 根菜单），保证授权后菜单可见。
     */
    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/api/user/{id}/permissions")
    @Transactional
    public R savePermissions(@PathVariable Integer id, @RequestBody List<Integer> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            tUserPermissionMapper.deleteByUserId(id);
            return R.OK();
        }

        List<Integer> uniqueIds = permissionIds.stream().distinct().collect(Collectors.toList());
        List<TPermission> perms = tPermissionMapper.selectByIds(uniqueIds);
        if (perms.size() != uniqueIds.size()) {
            return R.FAIL("包含不存在的权限，保存已取消");
        }
        boolean hasNonAssignable = perms.stream()
                .anyMatch(p -> !Integer.valueOf(1).equals(p.getAssignable()));
        if (hasNonAssignable) {
            return R.FAIL("包含不可授权的权限（权限管理模块），保存已取消");
        }

        // 自动补齐菜单链，保证授权后侧边栏菜单可见
        List<Integer> finalIds = buildMenuChain(perms, tPermissionMapper.selectAll());

        tUserPermissionMapper.deleteByUserId(id);
        List<TUserPermission> list = new ArrayList<>();
        for (Integer permId : finalIds) {
            TUserPermission up = new TUserPermission();
            up.setUserId(id);
            up.setPermissionId(permId);
            list.add(up);
        }
        tUserPermissionMapper.insertBatch(list);
        return R.OK();
    }

    /**
     * 根据已选权限自动补齐菜单链：从每个权限逐级向上找父级菜单，
     * 保证用户授权后能看到对应的侧边栏菜单。
     */
    private List<Integer> buildMenuChain(List<TPermission> grantedPerms, List<TPermission> allPermissions) {
        Map<Integer, TPermission> permissionMap = allPermissions.stream()
                .collect(Collectors.toMap(TPermission::getId, p -> p, (a, b) -> a));
        Set<Integer> result = new LinkedHashSet<>();
        for (TPermission perm : grantedPerms) {
            result.add(perm.getId());
            Integer parentId = perm.getParentId();
            while (parentId != null && parentId != 0) {
                TPermission parent = permissionMap.get(parentId);
                if (parent == null) {
                    break;
                }
                if ("menu".equals(parent.getType())) {
                    result.add(parent.getId());
                }
                parentId = parent.getParentId();
            }
        }
        return new ArrayList<>(result);
    }
}
