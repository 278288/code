package com.bjpowernode.service.impl;

import com.alibaba.excel.EasyExcel;
import com.bjpowernode.config.listener.UploadDataListener;
import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TClueMapper;
import com.bjpowernode.mapper.TClueRemarkMapper;
import com.bjpowernode.mapper.TCustomerMapper;
import com.bjpowernode.model.TClue;
import com.bjpowernode.query.BaseQuery;
import com.bjpowernode.query.ClueQuery;
import com.bjpowernode.service.ClueService;
import com.bjpowernode.util.JWTUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 线索管理服务实现。
 * 支持分页查询、Excel 批量导入、手机号查重、新增、编辑、详情查询、删除。
 * 新增线索前校验手机号唯一性。
 */
@Service
public class ClueServiceImpl implements ClueService {

    @Resource
    private TClueMapper tClueMapper;

    @Resource
    private TClueRemarkMapper tClueRemarkMapper;

    @Resource
    private TCustomerMapper tCustomerMapper;

    @Override
    public PageInfo<TClue> getClueByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TClue> list = tClueMapper.selectClueByPage(BaseQuery.builder().build());
        PageInfo<TClue> info = new PageInfo<>(list);
        return info;
    }

    /**
     * Excel 批量导入线索。使用 EasyExcel 读取流，UploadDataListener 逐批写入数据库。
     */
    @Override
    public void importExcel(InputStream inputStream, String token) {
        EasyExcel.read(inputStream, TClue.class, new UploadDataListener(tClueMapper, token))
                .sheet()
                .doRead();
    }

    /**
     * 校验手机号是否已存在。返回 true 表示可用（未录入过）。
     */
    @Override
    public Boolean checkPhone(String phone) {
        int count = tClueMapper.selectByCount(phone);
        return count <= 0;
    }

    /**
     * 新增线索。先查重手机号，已存在则抛异常，否则写入。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveClue(ClueQuery clueQuery) {
        int count = tClueMapper.selectByCount(clueQuery.getPhone());
        if (count <= 0) {
            TClue tClue = new TClue();
            BeanUtils.copyProperties(clueQuery, tClue);
            Integer loginUserId = JWTUtils.parseUserFromJWT(clueQuery.getToken()).getId();
            tClue.setCreateTime(new Date());
            tClue.setCreateBy(loginUserId);
            return tClueMapper.insertSelective(tClue);
        } else {
            throw new RuntimeException("该手机号已经录入过了，不能再录入");
        }
    }

    @Override
    public TClue getClueById(Integer id) {
        return tClueMapper.selectDetailById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateClue(ClueQuery clueQuery) {
        TClue tClue = new TClue();
        BeanUtils.copyProperties(clueQuery, tClue);
        Integer loginUserId = JWTUtils.parseUserFromJWT(clueQuery.getToken()).getId();
        tClue.setEditTime(new Date());
        tClue.setEditBy(loginUserId);
        return tClueMapper.updateByPrimaryKeySelective(tClue);
    }

    /**
     * 删除线索。如果线索已转化为客户则不允许删除，否则先删除跟踪记录再删除线索。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteClue(Integer id) {
        // 检查是否有关联的客户记录
        int customerCount = tCustomerMapper.selectCountByClueId(id);
        if (customerCount > 0) {
            throw new RuntimeException("该线索已转化为客户，无法删除");
        }
        // 删除线索的所有跟踪记录
        tClueRemarkMapper.deleteByClueId(id);
        // 删除线索
        return tClueMapper.deleteByPrimaryKey(id);
    }
}
