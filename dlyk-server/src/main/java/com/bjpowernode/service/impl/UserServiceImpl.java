package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.manager.RedisManager;
import com.bjpowernode.mapper.TUserPermissionMapper;
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

/**
 * 用户管理服务实现，同时实现 Spring Security 的 UserDetailsService。
 *
 * loadUserByUsername 是认证核心方法，Spring Security 调用它来获取用户信息（含角色和权限）。
 * 用户密码使用 BCrypt 加密存储，修改密码时需要 encode 新密码。
 */
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

    @Resource
    private TUserPermissionMapper tUserPermissionMapper;

    /**
     * Spring Security 认证入口。
     * 登录时被框架调用，根据 loginAct 查询用户，并加载其角色和权限列表。
     *
     * @param username 登录账号（loginAct）
     * @return Spring Security UserDetails 对象（TUser 实现了该接口）
     * @throws UsernameNotFoundException 用户不存在时抛出，触发认证失败
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        TUser tUser = tUserMapper.selectByLoginAct(username);
        if (tUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 加载角色
        List<TRole> tRoleList = tRoleMapper.selectByUserId(tUser.getId());
        List<String> stringRoleList = new ArrayList<>();
        tRoleList.forEach(tRole -> {
            stringRoleList.add(tRole.getRole());
        });
        tUser.setRoleList(stringRoleList);

        // 加载菜单权限（前端菜单渲染用，管理员通过角色数据获得权限管理菜单）
        List<TPermission> menuPermissionList = tPermissionMapper.selectMenuPermissionByUserId(tUser.getId());
        // 按orderNo排序，保证侧边栏菜单顺序稳定
        menuPermissionList.sort((a, b) -> {
            int orderA = a.getOrderNo() != null ? a.getOrderNo() : 0;
            int orderB = b.getOrderNo() != null ? b.getOrderNo() : 0;
            return Integer.compare(orderA, orderB);
        });
        tUser.setMenuPermissionList(menuPermissionList);

        // 加载按钮权限（前端按钮显隐用）
        List<TPermission> buttonPermissionList = tPermissionMapper.selectButtonPermissionByUserId(tUser.getId());
        List<String> stringPermissionList = new ArrayList<>();
        buttonPermissionList.forEach(tPermission -> {
            if (tPermission.getCode() != null && !tPermission.getCode().isEmpty()) {
                stringPermissionList.add(tPermission.getCode());
            }
        });
        // 加载用户个人权限（补充角色权限），合并到 permissionList
        List<Integer> userPermIds = tUserPermissionMapper.selectPermissionIdsByUserId(tUser.getId());
        if (userPermIds != null && !userPermIds.isEmpty()) {
            List<TPermission> userPerms = tPermissionMapper.selectByIds(userPermIds);
            if (userPerms != null) {
                userPerms.forEach(perm -> {
                    // 只合并可授权权限（assignable=1），避免历史脏数据把权限管理模块的权限带给普通用户
                    if (Integer.valueOf(1).equals(perm.getAssignable())
                            && perm.getCode() != null
                            && !perm.getCode().isEmpty()
                            && !stringPermissionList.contains(perm.getCode())) {
                        stringPermissionList.add(perm.getCode());
                    }
                });
            }
        }
        tUser.setPermissionList(stringPermissionList);

        return tUser;
    }

    /**
     * 分页查询用户列表。
     */
    @Override
    public PageInfo<TUser> getUserByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TUser> list = tUserMapper.selectUserByPage(BaseQuery.builder().build());
        PageInfo<TUser> info = new PageInfo<>(list);
        return info;
    }

    /**
     * 查询用户详情（含角色信息）。
     */
    @Override
    public TUser getUserById(Integer id) {
        return tUserMapper.selectDetailById(id);
    }

    /**
     * 新增用户。密码使用 BCrypt 加密后存储。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveUser(UserQuery userQuery) {
        TUser tUser = new TUser();
        BeanUtils.copyProperties(userQuery, tUser);
        tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd()));
        tUser.setCreateTime(new Date());
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setCreateBy(loginUserId);
        int result = tUserMapper.insertSelective(tUser);
        // 新用户主键回填到 userQuery，供调用方继续使用（如保存角色）
        userQuery.setId(tUser.getId());
        // 新增用户后清除负责人缓存
        redisManager.delete(Constants.REDIS_OWNER_KEY);
        return result;
    }

    /**
     * 编辑用户。如果传入了新密码才重新加密，否则保持原密码。
     */
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
        int result = tUserMapper.deleteByPrimaryKey(id);
        // 删除用户后清除负责人缓存
        redisManager.delete(Constants.REDIS_OWNER_KEY);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelUserIds(List<String> idList) {
        return tUserMapper.deleteByIds(idList);
    }

    /**
     * 获取所有负责人列表（带缓存：先查 Redis，无则查 DB 并回写 Redis）。
     */
    @Override
    public List<TUser> getOwnerList() {
        return CacheUtils.getCacheData(() -> {
            return (List<TUser>) redisManager.getValue(Constants.REDIS_OWNER_KEY);
        },
                () -> {
                    return (List<TUser>) tUserMapper.selectByOwner();
                },
                (t) -> {
                    redisManager.setValue(Constants.REDIS_OWNER_KEY, t);
                });
    }

    /**
     * 我的资料：更新姓名、手机、邮箱。
     */
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

    /**
     * 修改密码：先验证旧密码，通过后加密新密码并更新。
     *
     * @return true 修改成功，false 旧密码错误或用户不存在
     */
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