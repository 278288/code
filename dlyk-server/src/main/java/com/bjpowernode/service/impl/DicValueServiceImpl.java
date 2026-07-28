package com.bjpowernode.service.impl;

import com.bjpowernode.DlykServerApplication;
import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TDicValueMapper;
import com.bjpowernode.model.TDicValue;
import com.bjpowernode.query.DicValueQuery;
import com.bjpowernode.service.DicValueService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典值管理服务实现。
 *
 * 字典值缓存在 DlykServerApplication.cacheMap 中（按 typeCode 分组），
 * 增、删、改操作后需调用 refreshCache 刷新内存缓存，保证数据和缓存一致。
 */
@Service
public class DicValueServiceImpl implements DicValueService {

    @Resource
    private TDicValueMapper tDicValueMapper;

    /**
     * 分页查询字典值。传入 typeCode 按类型筛选，否则查全部。
     */
    @Override
    public PageInfo<TDicValue> getDicValueByPage(Integer current, String typeCode) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TDicValue> list;
        if (StringUtils.hasText(typeCode)) {
            list = tDicValueMapper.selectByTypeCode(typeCode);
        } else {
            list = tDicValueMapper.selectAll();
        }
        return new PageInfo<>(list);
    }

    @Override
    public TDicValue getDicValueById(Integer id) {
        return tDicValueMapper.selectByPrimaryKey(id);
    }

    @Override
    public int saveDicValue(DicValueQuery dicValueQuery) {
        TDicValue tDicValue = new TDicValue();
        tDicValue.setTypeCode(dicValueQuery.getTypeCode());
        tDicValue.setTypeValue(dicValueQuery.getTypeValue());
        tDicValue.setOrder(dicValueQuery.getOrder());
        tDicValue.setRemark(dicValueQuery.getRemark());
        int result = tDicValueMapper.insertSelective(tDicValue);
        refreshCache(dicValueQuery.getTypeCode());
        return result;
    }

    @Override
    public int updateDicValue(DicValueQuery dicValueQuery) {
        TDicValue tDicValue = new TDicValue();
        tDicValue.setId(dicValueQuery.getId());
        tDicValue.setTypeCode(dicValueQuery.getTypeCode());
        tDicValue.setTypeValue(dicValueQuery.getTypeValue());
        tDicValue.setOrder(dicValueQuery.getOrder());
        tDicValue.setRemark(dicValueQuery.getRemark());
        int result = tDicValueMapper.updateByPrimaryKeySelective(tDicValue);
        refreshCache(dicValueQuery.getTypeCode());
        return result;
    }

    @Override
    public int deleteDicValueById(Integer id) {
        TDicValue dv = tDicValueMapper.selectByPrimaryKey(id);
        int result = tDicValueMapper.deleteByPrimaryKey(id);
        if (dv != null) {
            refreshCache(dv.getTypeCode());
        }
        return result;
    }

    @Override
    @Transactional
    public int batchDeleteDicValues(List<String> idList) {
        int count = 0;
        for (String id : idList) {
            Integer intId = Integer.parseInt(id);
            TDicValue dv = tDicValueMapper.selectByPrimaryKey(intId);
            count += tDicValueMapper.deleteByPrimaryKey(intId);
            if (dv != null) {
                refreshCache(dv.getTypeCode());
            }
        }
        return count;
    }

    /**
     * 刷新指定 typeCode 的字典值缓存。从数据库重新查询后放回 cacheMap。
     */
    private void refreshCache(String typeCode) {
        if (StringUtils.hasText(typeCode)) {
            List<TDicValue> list = tDicValueMapper.selectByTypeCode(typeCode);
            DlykServerApplication.cacheMap.put(typeCode, list);
        }
    }
}