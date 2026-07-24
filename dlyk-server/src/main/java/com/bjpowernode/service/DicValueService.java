package com.bjpowernode.service;

import com.bjpowernode.model.TDicValue;
import com.bjpowernode.query.DicValueQuery;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface DicValueService {

    PageInfo<TDicValue> getDicValueByPage(Integer current, String typeCode);

    TDicValue getDicValueById(Integer id);

    int saveDicValue(DicValueQuery dicValueQuery);

    int updateDicValue(DicValueQuery dicValueQuery);

    int deleteDicValueById(Integer id);

    int batchDeleteDicValues(List<String> idList);
}