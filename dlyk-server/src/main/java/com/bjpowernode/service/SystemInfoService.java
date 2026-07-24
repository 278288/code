package com.bjpowernode.service;

import com.bjpowernode.model.TSystemInfo;
import com.bjpowernode.query.SystemInfoQuery;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface SystemInfoService {

    PageInfo<TSystemInfo> getSystemInfoByPage(Integer current);

    TSystemInfo getSystemInfoById(Integer id);

    int saveSystemInfo(SystemInfoQuery query);

    int updateSystemInfo(SystemInfoQuery query);

    int deleteSystemInfoById(Integer id);

    int batchDeleteSystemInfos(List<String> idList);
}