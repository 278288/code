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
 * Excel 是否需要贷款 → Java ID 转换器。
 * 例如："需要" → 49，"不需要" → 对应字典值 ID。
 */
public class NeedLoanConverter implements Converter<Integer> {

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String cellNeedLoanName = cellData.getStringValue();

        List<TDicValue> tDicValueList =
                (List<TDicValue>) DlykServerApplication.cacheMap.get(DicEnum.NEEDLOAN.getCode());
        for (TDicValue tDicValue : tDicValueList) {
            if (cellNeedLoanName.equals(tDicValue.getTypeValue())) {
                return tDicValue.getId();
            }
        }
        return -1;
    }
}