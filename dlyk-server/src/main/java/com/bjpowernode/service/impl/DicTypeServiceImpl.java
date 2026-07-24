package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TDicTypeMapper;
import com.bjpowernode.model.TDicType;
import com.bjpowernode.query.DicTypeQuery;
import com.bjpowernode.service.DicTypeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DicTypeServiceImpl implements DicTypeService {

    @Resource
    private TDicTypeMapper tDicTypeMapper;

    @Override
    public List<TDicType> loadAllDicData() {
        return tDicTypeMapper.selectByAll();
    }

    @Override
    public PageInfo<TDicType> getDicTypeByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TDicType> list = tDicTypeMapper.selectByAll();
        return new PageInfo<>(list);
    }

    @Override
    public TDicType getDicTypeById(Integer id) {
        return tDicTypeMapper.selectByPrimaryKey(id);
    }

    @Override
    public int saveDicType(DicTypeQuery dicTypeQuery) {
        TDicType tDicType = new TDicType();
        tDicType.setTypeCode(dicTypeQuery.getTypeCode());
        tDicType.setTypeName(dicTypeQuery.getTypeName());
        tDicType.setRemark(dicTypeQuery.getRemark());
        return tDicTypeMapper.insertSelective(tDicType);
    }

    @Override
    public int updateDicType(DicTypeQuery dicTypeQuery) {
        TDicType tDicType = new TDicType();
        tDicType.setId(dicTypeQuery.getId());
        tDicType.setTypeCode(dicTypeQuery.getTypeCode());
        tDicType.setTypeName(dicTypeQuery.getTypeName());
        tDicType.setRemark(dicTypeQuery.getRemark());
        return tDicTypeMapper.updateByPrimaryKeySelective(tDicType);
    }

    @Override
    public int deleteDicTypeById(Integer id) {
        return tDicTypeMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int batchDeleteDicTypes(List<String> idList) {
        int count = 0;
        for (String id : idList) {
            count += tDicTypeMapper.deleteByPrimaryKey(Integer.parseInt(id));
        }
        return count;
    }
}