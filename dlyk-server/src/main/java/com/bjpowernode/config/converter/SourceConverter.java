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
 * Excel 线索来源 → Java ID 转换器。
 * 例如："车展会" → 3，"网络广告" → 16。
 */
public class SourceConverter implements Converter<Integer> {

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String cellSourceName = cellData.getStringValue();

        List<TDicValue> tDicValueList =
                (List<TDicValue>) DlykServerApplication.cacheMap.get(DicEnum.SOURCE.getCode());
        for (TDicValue tDicValue : tDicValueList) {
            if (cellSourceName.equals(tDicValue.getTypeValue())) {
                return tDicValue.getId();
            }
        }
        return -1;
    }
}