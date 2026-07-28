package com.bjpowernode.web;

import com.bjpowernode.model.TDicType;
import com.bjpowernode.query.DicTypeQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.DicTypeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class DicTypeController {

    @Resource
    private DicTypeService dicTypeService;

    @GetMapping(value = "/api/dictypes")
    public R dicTypePage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) { current = 1; }
        PageInfo<TDicType> pageInfo = dicTypeService.getDicTypeByPage(current);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/dictype/{id}")
    public R dicTypeDetail(@PathVariable(value = "id") Integer id) {
        TDicType tDicType = dicTypeService.getDicTypeById(id);
        return R.OK(tDicType);
    }

    @PostMapping(value = "/api/dictype")
    public R addDicType(@Valid DicTypeQuery dicTypeQuery) {
        int save = dicTypeService.saveDicType(dicTypeQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/dictype")
    public R editDicType(@Valid DicTypeQuery dicTypeQuery) {
        int update = dicTypeService.updateDicType(dicTypeQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/dictype/{id}")
    public R delDicType(@PathVariable(value = "id") Integer id) {
        int del = dicTypeService.deleteDicTypeById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/dictype")
    public R batchDelDicType(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = dicTypeService.batchDeleteDicTypes(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}