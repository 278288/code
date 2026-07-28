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
 * Excel 线索状态 → Java ID 转换器。
 * 例如："已联系" → 27，"未联系" → 对应字典值 ID。
 */
public class StateConverter implements Converter<Integer> {

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String cellStateName = cellData.getStringValue();

        List<TDicValue> tDicValueList =
                (List<TDicValue>) DlykServerApplication.cacheMap.get(DicEnum.STATE.getCode());
        for (TDicValue tDicValue : tDicValueList) {
            if (cellStateName.equals(tDicValue.getTypeValue())) {
                return tDicValue.getId();
            }
        }
        return -1;
    }
}