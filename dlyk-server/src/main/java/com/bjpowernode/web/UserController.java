package com.bjpowernode.web;

import com.bjpowernode.model.TUser;
import com.bjpowernode.query.UserQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户管理 Controller。
 *
 * 接口列表：
 *   GET    /api/login/info      获取当前登录用户信息
 *   GET    /api/login/free      免登录接口（测试用）
 *   GET    /api/users           分页查询用户列表（需 user:list 权限）
 *   GET    /api/user/{id}       用户详情（需 user:view 权限）
 *   POST   /api/user            新增用户（需 user:add 权限）
 *   PUT    /api/user            编辑用户（需 user:edit 权限）
 *   DELETE /api/user/{id}       删除用户（需 user:delete 权限）
 *   DELETE /api/user            批量删除（需 user:delete 权限）
 *   GET    /api/owner           获取负责人列表（下拉选择用）
 *   PUT    /api/user/profile    我的资料（修改姓名、手机、邮箱）
 *   PUT    /api/user/password   修改密码（需验证旧密码）
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    /** 获取当前登录用户信息（前端 header 展示用） */
    @GetMapping(value = "/api/login/info")
    public R loginInfo(Authentication authentication) {
        TUser tUser = (TUser) authentication.getPrincipal();
        return R.OK(tUser);
    }

    /** 免登录测试接口 */
    @GetMapping(value = "/api/login/free")
    public R freeLogin() {
        return R.OK();
    }

    @PreAuthorize(value = "hasAuthority('user:list')")
    @GetMapping(value = "/api/users")
    public R userPage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) {
            current = 1;
        }
        PageInfo<TUser> pageInfo = userService.getUserByPage(current);
        return R.OK(pageInfo);
    }

    @PreAuthorize(value = "hasAuthority('user:view')")
    @GetMapping(value = "/api/user/{id}")
    public R userDetail(@PathVariable(value = "id") Integer id) {
        TUser tUser = userService.getUserById(id);
        return R.OK(tUser);
    }

    @PreAuthorize(value = "hasAuthority('user:add')")
    @PostMapping(value = "/api/user")
    public R addUser(@Valid UserQuery userQuery, @RequestHeader(value = "Authorization") String token) {
        userQuery.setToken(token);
        int save = userService.saveUser(userQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PreAuthorize(value = "hasAuthority('user:edit')")
    @PutMapping(value = "/api/user")
    public R editUser(@Valid UserQuery userQuery, @RequestHeader(value = "Authorization") String token) {
        userQuery.setToken(token);
        int update = userService.updateUser(userQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @PreAuthorize(value = "hasAuthority('user:delete')")
    @DeleteMapping(value = "/api/user/{id}")
    public R delUser(@PathVariable(value = "id") Integer id) {
        int del = userService.delUserById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @PreAuthorize(value = "hasAuthority('user:delete')")
    @DeleteMapping(value = "/api/user")
    public R batchDelUser(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = userService.batchDelUserIds(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }

    /** 获取负责人列表（线索、客户归属下拉选择用） */
    @GetMapping(value = "/api/owner")
    public R owner() {
        List<TUser> ownerList = userService.getOwnerList();
        return R.OK(ownerList);
    }

    /** 我的资料（修改姓名、手机、邮箱） */
    @PutMapping(value = "/api/user/profile")
    public R updateProfile(@Valid UserQuery userQuery) {
        int update = userService.updateProfile(userQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    /** 修改密码：先验证旧密码，通过后再更新 */
    @PutMapping(value = "/api/user/password")
    public R changePassword(@RequestParam("oldPwd") String oldPwd,
                             @RequestParam("newPwd") String newPwd,
                             Authentication authentication) {
        TUser currentUser = (TUser) authentication.getPrincipal();
        boolean result = userService.changePassword(currentUser.getId(), oldPwd, newPwd);
        return result ? R.OK() : R.FAIL("原密码错误");
    }
}