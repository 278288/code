package com.bjpowernode.web;

import com.bjpowernode.model.TSystemInfo;
import com.bjpowernode.query.SystemInfoQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.SystemInfoService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 系统信息 Controller。
 * 管理网站 SEO 元信息（标题、描述、关键词、Logo 等），支持增删改查和批量删除。
 */
@RestController
public class SystemInfoController {

    @Resource
    private SystemInfoService systemInfoService;

    @GetMapping(value = "/api/systemInfos")
    public R systemInfoPage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) { current = 1; }
        PageInfo<TSystemInfo> pageInfo = systemInfoService.getSystemInfoByPage(current);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/systemInfo/{id}")
    public R systemInfoDetail(@PathVariable(value = "id") Integer id) {
        TSystemInfo info = systemInfoService.getSystemInfoById(id);
        return R.OK(info);
    }

    @PostMapping(value = "/api/systemInfo")
    public R addSystemInfo(@Valid SystemInfoQuery query) {
        int save = systemInfoService.saveSystemInfo(query);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/systemInfo")
    public R editSystemInfo(@Valid SystemInfoQuery query) {
        int update = systemInfoService.updateSystemInfo(query);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/systemInfo/{id}")
    public R delSystemInfo(@PathVariable(value = "id") Integer id) {
        int del = systemInfoService.deleteSystemInfoById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/systemInfo")
    public R batchDelSystemInfo(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = systemInfoService.batchDeleteSystemInfos(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}