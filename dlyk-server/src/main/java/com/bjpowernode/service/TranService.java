package com.bjpowernode.service;

import com.bjpowernode.model.TTran;
import com.bjpowernode.query.TranQuery;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface TranService {

    PageInfo<TTran> getTranByPage(Integer current);

    TTran getTranById(Integer id);

    int saveTran(TranQuery tranQuery);

    int updateTran(TranQuery tranQuery);

    int deleteTranById(Integer id);

    int batchDeleteTrans(List<String> idList);
}