package com.bjpowernode.web;

import com.bjpowernode.model.TTran;
import com.bjpowernode.query.TranQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.TranService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 交易管理 Controller。
 *
 * 接口列表：
 *   GET    /api/trans      分页查询交易
 *   GET    /api/tran/{id}   交易详情（含客户名、手机号）
 *   POST   /api/tran        新增交易（同时创建一条初始历史记录）
 *   PUT    /api/tran        编辑交易（阶段变化时追加历史记录）
 *   DELETE /api/tran/{id}   删除交易（含子表历史记录和备注）
 *   DELETE /api/tran        批量删除（ids 逗号分隔）
 */
@RestController
public class TranController {

    @Resource
    private TranService tranService;

    /** 分页查询交易列表 */
    @GetMapping(value = "/api/trans")
    public R tranPage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) {
            current = 1;
        }
        PageInfo<TTran> pageInfo = tranService.getTranByPage(current);
        return R.OK(pageInfo);
    }

    /** 交易详情（含客户名称、手机号等关联信息） */
    @GetMapping(value = "/api/tran/{id}")
    public R tranDetail(@PathVariable(value = "id") Integer id) {
        TTran tTran = tranService.getTranById(id);
        return R.OK(tTran);
    }

    /** 新增交易，@Valid 校验入参非空 */
    @PostMapping(value = "/api/tran")
    public R addTran(@Valid TranQuery tranQuery, @RequestHeader(value = "Authorization") String token) {
        tranQuery.setToken(token);
        int save = tranService.saveTran(tranQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    /** 编辑交易，阶段变更时自动追加历史 */
    @PutMapping(value = "/api/tran")
    public R editTran(@Valid TranQuery tranQuery, @RequestHeader(value = "Authorization") String token) {
        tranQuery.setToken(token);
        int update = tranService.updateTran(tranQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    /** 删除单条交易（含关联子表数据） */
    @DeleteMapping(value = "/api/tran/{id}")
    public R delTran(@PathVariable(value = "id") Integer id) {
        int del = tranService.deleteTranById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    /** 批量删除交易，ids 逗号分隔 */
    @DeleteMapping(value = "/api/tran")
    public R batchDelTran(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = tranService.batchDeleteTrans(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}