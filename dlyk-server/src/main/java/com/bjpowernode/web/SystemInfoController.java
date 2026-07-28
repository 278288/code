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

@RestController
public class SystemInfoController {

    @Resource
    private SystemInfoService systemInfoService;

    @GetMapping(value = "/api/systems")
    public R systemPage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) { current = 1; }
        PageInfo<TSystemInfo> pageInfo = systemInfoService.getSystemInfoByPage(current);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/system/{id}")
    public R systemDetail(@PathVariable(value = "id") Integer id) {
        TSystemInfo info = systemInfoService.getSystemInfoById(id);
        return R.OK(info);
    }

    @PostMapping(value = "/api/system")
    public R addSystem(@Valid SystemInfoQuery query) {
        int save = systemInfoService.saveSystemInfo(query);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/system")
    public R editSystem(@Valid SystemInfoQuery query) {
        int update = systemInfoService.updateSystemInfo(query);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/system/{id}")
    public R delSystem(@PathVariable(value = "id") Integer id) {
        int del = systemInfoService.deleteSystemInfoById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/system")
    public R batchDelSystem(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = systemInfoService.batchDeleteSystemInfos(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}