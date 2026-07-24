package com.bjpowernode.web;

import com.bjpowernode.model.TTran;
import com.bjpowernode.query.TranQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.TranService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class TranController {

    @Resource
    private TranService tranService;

    @GetMapping(value = "/api/trans")
    public R tranPage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) {
            current = 1;
        }
        PageInfo<TTran> pageInfo = tranService.getTranByPage(current);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/tran/{id}")
    public R tranDetail(@PathVariable(value = "id") Integer id) {
        TTran tTran = tranService.getTranById(id);
        return R.OK(tTran);
    }

    @PostMapping(value = "/api/tran")
    public R addTran(TranQuery tranQuery, @RequestHeader(value = "Authorization") String token) {
        tranQuery.setToken(token);
        int save = tranService.saveTran(tranQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/tran")
    public R editTran(TranQuery tranQuery, @RequestHeader(value = "Authorization") String token) {
        tranQuery.setToken(token);
        int update = tranService.updateTran(tranQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/tran/{id}")
    public R delTran(@PathVariable(value = "id") Integer id) {
        int del = tranService.deleteTranById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/tran")
    public R batchDelTran(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = tranService.batchDeleteTrans(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}