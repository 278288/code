package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TActivityMapper;
import com.bjpowernode.model.TActivity;
import com.bjpowernode.query.ActivityQuery;
import com.bjpowernode.service.ActivityService;
import com.bjpowernode.util.JWTUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 市场活动管理服务实现。
 * 支持分页查询、新增、更新、详情查询，以及获取进行中活动列表（线索转化时选择活动）。
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private TActivityMapper tActivityMapper;

    /**
     * 分页查询市场活动列表（支持按条件筛选）。
     */
    @Override
    public PageInfo<TActivity> getActivityByPage(Integer current, ActivityQuery activityQuery) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TActivity> list = tActivityMapper.selectActivityByPage(activityQuery);
        PageInfo<TActivity> info = new PageInfo<>(list);
        return info;
    }

    /**
     * 新增市场活动。创建人从 JWT 中解析，负责人由用户从下拉列表选择。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveActivity(ActivityQuery activityQuery) {
        TActivity tActivity = new TActivity();
        BeanUtils.copyProperties(activityQuery, tActivity);
        tActivity.setCreateTime(new Date());
        Integer loginUserId = JWTUtils.parseUserFromJWT(activityQuery.getToken()).getId();
        tActivity.setCreateBy(loginUserId);
        // ownerId 由 BeanUtils.copyProperties 从表单中复制，不需要手动设置
        return tActivityMapper.insertSelective(tActivity);
    }

    @Override
    public TActivity getActivityById(Integer id) {
        return tActivityMapper.selectDetailByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateActivity(ActivityQuery activityQuery) {
        TActivity tActivity = new TActivity();
        BeanUtils.copyProperties(activityQuery, tActivity);
        tActivity.setEditTime(new Date());
        Integer loginUserId = JWTUtils.parseUserFromJWT(activityQuery.getToken()).getId();
        tActivity.setEditBy(loginUserId);
        return tActivityMapper.updateByPrimaryKeySelective(tActivity);
    }

    /**
     * 获取所有进行中的市场活动（增量选择器下拉用）。
     */
    @Override
    public List<TActivity> getOngoingActivity() {
        return tActivityMapper.selecOngoingActivity();
    }
}