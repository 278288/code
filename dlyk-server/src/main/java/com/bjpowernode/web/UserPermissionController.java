package com.bjpowernode.web;

import com.bjpowernode.mapper.TUserPermissionMapper;
import com.bjpowernode.model.TUserPermission;
import com.bjpowernode.result.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户权限管理 Controller。
 * 管理员为用户单独授权模块权限（补充角色权限）。
 *
 * GET  /api/user/{id}/permissions  查询用户当前个人权限 ID 列表
 * PUT  /api/user/{id}/permissions  保存用户个人权限（先删后插，body: [1,2,3,...]）
 */
@RestController
public class UserPermissionController {

    @Resource
    private TUserPermissionMapper tUserPermissionMapper;

    @PreAuthorize("hasAuthority('user:edit')")
    @GetMapping("/api/user/{id}/permissions")
    public R getPermissions(@PathVariable Integer id) {
        List<Integer> permIds = tUserPermissionMapper.selectPermissionIdsByUserId(id);
        return R.OK(permIds);
    }

    @PreAuthorize("hasAuthority('user:edit')")
    @PutMapping("/api/user/{id}/permissions")
    @Transactional
    public R savePermissions(@PathVariable Integer id, @RequestBody List<Integer> permissionIds) {
        // 先删后插，全量替换
        tUserPermissionMapper.deleteByUserId(id);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<TUserPermission> list = new ArrayList<>();
            for (Integer permId : permissionIds) {
                TUserPermission up = new TUserPermission();
                up.setUserId(id);
                up.setPermissionId(permId);
                list.add(up);
            }
            tUserPermissionMapper.insertBatch(list);
        }
        return R.OK();
    }
}