package com.bjpowernode.service;

import com.bjpowernode.model.TDicType;
import com.bjpowernode.query.DicTypeQuery;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface DicTypeService {

    List<TDicType> loadAllDicData();

    PageInfo<TDicType> getDicTypeByPage(Integer current);

    TDicType getDicTypeById(Integer id);

    int saveDicType(DicTypeQuery dicTypeQuery);

    int updateDicType(DicTypeQuery dicTypeQuery);

    int deleteDicTypeById(Integer id);

    int batchDeleteDicTypes(List<String> idList);
}