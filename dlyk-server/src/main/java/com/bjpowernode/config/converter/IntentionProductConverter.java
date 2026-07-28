package com.bjpowernode.config.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.bjpowernode.DlykServerApplication;
import com.bjpowernode.model.TProduct;
import com.bjpowernode.result.DicEnum;

import java.util.List;

/**
 * Excel 意向产品名 → Java 产品 ID 转换器。
 * 从产品缓存中查询对应 ID，匹配不到返回 -1。
 */
public class IntentionProductConverter implements Converter<Integer> {

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String cellIntentionProductName = cellData.getStringValue();

        List<TProduct> tDicValueList =
                (List<TProduct>) DlykServerApplication.cacheMap.get(DicEnum.PRODUCT.getCode());
        for (TProduct tProduct : tDicValueList) {
            if (cellIntentionProductName.equals(tProduct.getName())) {
                return tProduct.getId();
            }
        }
        return -1;
    }
}