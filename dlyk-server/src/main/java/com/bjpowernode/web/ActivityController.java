package com.bjpowernode.web;

import com.bjpowernode.model.TActivity;
import com.bjpowernode.query.ActivityQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.ActivityService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
 * 市场活动 Controller。
 *
 * 接口列表：
 *   GET  /api/activitys       分页查询（支持条件筛选）
 *   POST /api/activity        新增活动
 *   GET  /api/activity/{id}   活动详情
 *   PUT  /api/activity        编辑活动
 */
@RestController
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @GetMapping(value = "/api/activitys")
    public R activityPage(@RequestParam(value = "current", required = false) Integer current,
                           ActivityQuery activityQuery) {
        if (current == null) {
            current = 1;
        }
        PageInfo<TActivity> pageInfo = activityService.getActivityByPage(current, activityQuery);
        return R.OK(pageInfo);
    }

    @PostMapping(value = "/api/activity")
    public R addActivity(@Valid ActivityQuery activityQuery,
                          @RequestHeader(value = "Authorization") String token) {
        activityQuery.setToken(token);
        int save = activityService.saveActivity(activityQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @GetMapping(value = "/api/activity/{id}")
    public R loadActivity(@PathVariable(value = "id") Integer id) {
        TActivity tActivity = activityService.getActivityById(id);
        return R.OK(tActivity);
    }

    @PutMapping(value = "/api/activity")
    public R editActivity(@Valid ActivityQuery activityQuery,
                           @RequestHeader(value = "Authorization") String token) {
        activityQuery.setToken(token);
        int update = activityService.updateActivity(activityQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/activity/{id}")
    public R delActivity(@PathVariable(value = "id") Integer id) {
        int del = activityService.delActivityById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/activity")
    public R batchDelActivity(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = activityService.batchDelActivityByIds(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}