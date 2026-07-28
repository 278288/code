package com.bjpowernode.web;

import com.bjpowernode.DlykServerApplication;
import com.bjpowernode.model.TActivity;
import com.bjpowernode.model.TDicValue;
import com.bjpowernode.model.TProduct;
import com.bjpowernode.result.DicEnum;
import com.bjpowernode.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典数据查询 Controller。
 * 从应用缓存（cacheMap）中按 typeCode 取字典值或产品/活动列表，供前端下拉选择器使用。
 */
@RestController
public class DicController {

    @GetMapping(value = "/api/dicvalue/{typeCode}")
    public R dicData(@PathVariable(value = "typeCode") String typeCode) {
        if (typeCode.equals(DicEnum.ACTIVITY.getCode())) {
            List<TActivity> tActivityList = (List<TActivity>) DlykServerApplication.cacheMap.get(typeCode);
            if (tActivityList == null) {
                return R.FAIL("未找到活动数据");
            }
            return R.OK(tActivityList);
        } else {
            List<TDicValue> tDicValueList = (List<TDicValue>) DlykServerApplication.cacheMap.get(typeCode);
            if (tDicValueList == null) {
                return R.FAIL("未找到字典数据: " + typeCode);
            }
            return R.OK(tDicValueList);
        }
    }
}