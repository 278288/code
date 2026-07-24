package com.bjpowernode.web;

import com.bjpowernode.model.TUser;
import com.bjpowernode.query.UserQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 鑾峰彇鐧诲綍浜轰俊鎭?
     *
     * @param authentication
     * @return
     */
    @GetMapping(value = "/api/login/info")
    public R loginInfo(Authentication authentication) {
        TUser tUser = (TUser)authentication.getPrincipal();
        return R.OK(tUser);
    }

    /**
     * 鍏嶇櫥褰?
     *
     * @return
     */
    @GetMapping(value = "/api/login/free")
    public R freeLogin() {
        return R.OK();
    }

    /**
     * 鐢ㄦ埛鍒楄〃鍒嗛〉鏌ヨ
     *
     * @param current
     * @return
     */
    @PreAuthorize(value = "hasAuthority('user:list')")
    @GetMapping(value = "/api/users")
    public R userPage(@RequestParam(value = "current", required = false) Integer current) {
        //required = false 琛ㄧず鍙傛暟鍙互浼狅紝涔熷彲浠ヤ笉浼狅紱
        //required = true 琛ㄧず鍙傛暟蹇呴』瑕佷紶锛屼笉浼犱細鎶ラ敊锛?
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

    /**
     * 鏂板鐢ㄦ埛
     *
     * @param userQuery
     * @return
     */
    @PreAuthorize(value = "hasAuthority('user:add')")
    @PostMapping(value = "/api/user")
    public R addUser(UserQuery userQuery, @RequestHeader(value = "Authorization") String token) {
        userQuery.setToken(token);
        int save = userService.saveUser(userQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    /**
     * 缂栬緫鐢ㄦ埛
     *
     * @param userQuery
     * @return
     */
    @PreAuthorize(value = "hasAuthority('user:edit')")
    @PutMapping(value = "/api/user")
    public R editUser(UserQuery userQuery, @RequestHeader(value = "Authorization") String token) {
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
        //ids = "1,3,5,6,7,11,15";
        List<String> idList = Arrays.asList(ids.split(","));

        int batchDel = userService.batchDelUserIds(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }

    @GetMapping(value = "/api/owner")
    public R owner() {
        List<TUser> ownerList = userService.getOwnerList();
        return R.OK(ownerList);
    }

    @PutMapping(value = "/api/user/profile")
    public R updateProfile(UserQuery userQuery) {
        int update = userService.updateProfile(userQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/user/password")
    public R changePassword(@RequestParam("oldPwd") String oldPwd,
                             @RequestParam("newPwd") String newPwd,
                             Authentication authentication) {
        TUser currentUser = (TUser) authentication.getPrincipal();
        boolean result = userService.changePassword(currentUser.getId(), oldPwd, newPwd);
        return result ? R.OK() : R.FAIL("原密码错误");
    }
}