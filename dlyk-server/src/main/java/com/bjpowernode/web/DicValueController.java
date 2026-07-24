package com.bjpowernode.web;

import com.bjpowernode.model.TDicValue;
import com.bjpowernode.query.DicValueQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.DicValueService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class DicValueController {

    @Resource
    private DicValueService dicValueService;

    @GetMapping(value = "/api/dicvalues")
    public R dicValuePage(@RequestParam(value = "current", required = false) Integer current,
                          @RequestParam(value = "typeCode", required = false) String typeCode) {
        if (current == null) { current = 1; }
        PageInfo<TDicValue> pageInfo = dicValueService.getDicValueByPage(current, typeCode);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/dicvalue/{id}")
    public R dicValueDetail(@PathVariable(value = "id") Integer id) {
        TDicValue tDicValue = dicValueService.getDicValueById(id);
        return R.OK(tDicValue);
    }

    @PostMapping(value = "/api/dicvalue")
    public R addDicValue(DicValueQuery dicValueQuery) {
        int save = dicValueService.saveDicValue(dicValueQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/dicvalue")
    public R editDicValue(DicValueQuery dicValueQuery) {
        int update = dicValueService.updateDicValue(dicValueQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/dicvalue/{id}")
    public R delDicValue(@PathVariable(value = "id") Integer id) {
        int del = dicValueService.deleteDicValueById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/dicvalue")
    public R batchDelDicValue(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = dicValueService.batchDeleteDicValues(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}