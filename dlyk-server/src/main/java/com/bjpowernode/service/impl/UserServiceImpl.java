package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.manager.RedisManager;
import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.mapper.TRoleMapper;
import com.bjpowernode.mapper.TUserMapper;
import com.bjpowernode.model.TPermission;
import com.bjpowernode.model.TRole;
import com.bjpowernode.model.TUser;
import com.bjpowernode.query.BaseQuery;
import com.bjpowernode.query.UserQuery;
import com.bjpowernode.service.UserService;
import com.bjpowernode.util.CacheUtils;
import com.bjpowernode.util.JWTUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private TUserMapper tUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private TRoleMapper tRoleMapper;

    @Resource
    private RedisManager redisManager;

    @Resource
    private TPermissionMapper tPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        TUser tUser = tUserMapper.selectByLoginAct(username);
        if (tUser == null) {
            throw new UsernameNotFoundException("DDDDDDDD");
        }

        List<TRole> tRoleList = tRoleMapper.selectByUserId(tUser.getId());
        List<String> stringRoleList = new ArrayList<>();
        tRoleList.forEach(tRole -> {
            stringRoleList.add(tRole.getRole());
        });
        tUser.setRoleList(stringRoleList);

        List<TPermission> menuPermissionList = tPermissionMapper.selectMenuPermissionByUserId(tUser.getId());
        tUser.setMenuPermissionList(menuPermissionList);

        List<TPermission> buttonPermissionList = tPermissionMapper.selectButtonPermissionByUserId(tUser.getId());
        List<String> stringPermissionList = new ArrayList<>();
        buttonPermissionList.forEach(tPermission -> {
            stringPermissionList.add(tPermission.getCode());
        });
        tUser.setPermissionList(stringPermissionList);

        return tUser;
    }

    @Override
    public PageInfo<TUser> getUserByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TUser> list = tUserMapper.selectUserByPage(BaseQuery.builder().build());
        PageInfo<TUser> info = new PageInfo<>(list);
        return info;
    }

    @Override
    public TUser getUserById(Integer id) {
        return tUserMapper.selectDetailById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveUser(UserQuery userQuery) {
        TUser tUser = new TUser();
        BeanUtils.copyProperties(userQuery, tUser);
        tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd()));
        tUser.setCreateTime(new Date());
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setCreateBy(loginUserId);
        return tUserMapper.insertSelective(tUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUser(UserQuery userQuery) {
        TUser tUser = new TUser();
        BeanUtils.copyProperties(userQuery, tUser);
        if (StringUtils.hasText(userQuery.getLoginPwd())) {
            tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd()));
        }
        tUser.setEditTime(new Date());
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setEditBy(loginUserId);
        return tUserMapper.updateByPrimaryKeySelective(tUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int delUserById(Integer id) {
        return tUserMapper.deleteByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelUserIds(List<String> idList) {
        return tUserMapper.deleteByIds(idList);
    }

    @Override
    public List<TUser> getOwnerList() {
        return CacheUtils.getCacheData(() -> {
            return (List<TUser>)redisManager.getValue(Constants.REDIS_OWNER_KEY);
        },
        () -> {
            return (List<TUser>)tUserMapper.selectByOwner();
        },
        (t) -> {
            redisManager.setValue(Constants.REDIS_OWNER_KEY, t);
        }
       );
    }

    @Override
    public int updateProfile(UserQuery userQuery) {
        TUser tUser = new TUser();
        tUser.setId(userQuery.getId());
        tUser.setName(userQuery.getName());
        tUser.setPhone(userQuery.getPhone());
        tUser.setEmail(userQuery.getEmail());
        tUser.setEditTime(new Date());
        return tUserMapper.updateByPrimaryKeySelective(tUser);
    }

    @Override
    public boolean changePassword(Integer userId, String oldPwd, String newPwd) {
        TUser user = tUserMapper.selectByPrimaryKey(userId);
        if (user == null || !passwordEncoder.matches(oldPwd, user.getLoginPwd())) {
            return false;
        }
        TUser tUser = new TUser();
        tUser.setId(userId);
        tUser.setLoginPwd(passwordEncoder.encode(newPwd));
        tUser.setEditTime(new Date());
        return tUserMapper.updateByPrimaryKeySelective(tUser) >= 1;
    }
}