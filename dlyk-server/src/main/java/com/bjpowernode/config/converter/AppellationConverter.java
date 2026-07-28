package com.bjpowernode.config.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.bjpowernode.DlykServerApplication;
import com.bjpowernode.model.TDicValue;
import com.bjpowernode.result.DicEnum;

import java.util.List;

/**
 * Excel 称呼 → Java ID 转换器。
 * 例如：Excel 中的"先生" → Java 类中的 18，"女士" → 41。
 * 从应用缓存中查询对应 ID，匹配不到返回 -1。
 */
public class AppellationConverter implements Converter<Integer> {

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String cellAppellationName = cellData.getStringValue();

        List<TDicValue> tDicValueList =
                (List<TDicValue>) DlykServerApplication.cacheMap.get(DicEnum.APPELLATION.getCode());
        for (TDicValue tDicValue : tDicValueList) {
            if (cellAppellationName.equals(tDicValue.getTypeValue())) {
                return tDicValue.getId();
            }
        }
        return -1;
    }
}