package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.manager.RedisManager;
import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.mapper.TRoleMapper;
import com.bjpowernode.mapper.TUserMapper;
import com.bjpowernode.model.TActivityRemark;
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

    /**
     * 鐧诲綍鏌ヨ
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        TUser tUser = tUserMapper.selectByLoginAct(username);
        if (tUser == null) {
            throw new UsernameNotFoundException("鐧诲綍璐﹀彿涓嶅瓨鍦?);
        }

        //鏌ヨ涓€涓嬪綋鍓嶇敤鎴风殑瑙掕壊
        List<TRole> tRoleList = tRoleMapper.selectByUserId(tUser.getId());
        //瀛楃涓茬殑瑙掕壊鍒楄〃
        List<String> stringRoleList = new ArrayList<>();
        tRoleList.forEach(tRole -> {
            stringRoleList.add(tRole.getRole());
        });
        tUser.setRoleList(stringRoleList); //璁剧疆鐢ㄦ埛鐨勮鑹?

        //鏌ヨ涓€涓嬭鐢ㄦ埛鏈夊摢浜涜彍鍗曟潈闄?
        List<TPermission> menuPermissionList = tPermissionMapper.selectMenuPermissionByUserId(tUser.getId());
        tUser.setMenuPermissionList(menuPermissionList);

        //鏌ヨ涓€涓嬭鐢ㄦ埛鏈夊摢浜涘姛鑳芥潈闄?
        List<TPermission> buttonPermissionList = tPermissionMapper.selectButtonPermissionByUserId(tUser.getId());
        List<String> stringPermissionList = new ArrayList<>();
        buttonPermissionList.forEach(tPermission -> {
            stringPermissionList.add(tPermission.getCode());//鏉冮檺鏍囪瘑绗?
        });
        tUser.setPermissionList(stringPermissionList);//璁剧疆鐢ㄦ埛鐨勬潈闄愭爣璇嗙

        return tUser;
    }

    @Override
    public PageInfo<TUser> getUserByPage(Integer current) {
        // 1.璁剧疆PageHelper
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        // 2.鏌ヨ
        List<TUser> list = tUserMapper.selectUserByPage(BaseQuery.builder().build());
        // 3.灏佽鍒嗛〉鏁版嵁鍒癙ageInfo
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

        //鎶奤serQuery瀵硅薄閲岄潰鐨勫睘鎬ф暟鎹鍒跺埌TUser瀵硅薄閲岄潰鍘?澶嶅埗瑕佹眰锛氫袱涓璞＄殑灞炴€у悕鐩稿悓锛屽睘鎬х被鍨嬭鐩稿悓锛岃繖鏍锋墠鑳藉鍒?
        BeanUtils.copyProperties(userQuery, tUser);

        tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd())); //瀵嗙爜鍔犲瘑
        tUser.setCreateTime(new Date()); //鍒涘缓鏃堕棿

        //鐧诲綍浜虹殑id
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setCreateBy(loginUserId); //鍒涘缓浜?

        return tUserMapper.insertSelective(tUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUser(UserQuery userQuery) {
        TUser tUser = new TUser();

        //鎶奤serQuery瀵硅薄閲岄潰鐨勫睘鎬ф暟鎹鍒跺埌TUser瀵硅薄閲岄潰鍘?澶嶅埗瑕佹眰锛氫袱涓璞＄殑灞炴€у悕鐩稿悓锛屽睘鎬х被鍨嬭鐩稿悓锛岃繖鏍锋墠鑳藉鍒?
        BeanUtils.copyProperties(userQuery, tUser);

        if (StringUtils.hasText(userQuery.getLoginPwd())) {
            tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd())); //瀵嗙爜鍔犲瘑
        }

        tUser.setEditTime(new Date()); //缂栬緫鏃堕棿

        //鐧诲綍浜虹殑id
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setEditBy(loginUserId); //鍒涘缓浜?

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
        //1銆佷粠redis鏌ヨ
        //2銆乺edis鏌ヤ笉鍒帮紝灏变粠鏁版嵁搴撴煡璇紝骞朵笖鎶婃暟鎹斁鍏edis锛?鍒嗛挓杩囨湡锛?
        return CacheUtils.getCacheData(() -> {
            //鐢熶骇锛屼粠缂撳瓨redis鏌ヨ鏁版嵁
            return (List<TUser>)redisManager.getValue(Constants.REDIS_OWNER_KEY);
        },
        () -> {
            //鐢熶骇锛屼粠mysql鏌ヨ鏁版嵁
            return (List<TUser>)tUserMapper.selectByOwner();
        },
        (t) -> {
            //娑堣垂锛屾妸鏁版嵁鏀惧叆缂撳瓨redis
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