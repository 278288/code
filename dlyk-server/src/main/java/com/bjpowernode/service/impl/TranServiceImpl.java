package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TTranHistoryMapper;
import com.bjpowernode.mapper.TTranMapper;
import com.bjpowernode.mapper.TTranRemarkMapper;
import com.bjpowernode.model.TTran;
import com.bjpowernode.model.TTranHistory;
import com.bjpowernode.query.TranQuery;
import com.bjpowernode.service.TranService;
import com.bjpowernode.util.JWTUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class TranServiceImpl implements TranService {

    @Resource
    private TTranMapper tTranMapper;

    @Resource
    private TTranHistoryMapper tTranHistoryMapper;

    @Resource
    private TTranRemarkMapper tTranRemarkMapper;

    @Override
    public PageInfo<TTran> getTranByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TTran> list = tTranMapper.selectTranPage();
        return new PageInfo<>(list);
    }

    @Override
    public TTran getTranById(Integer id) {
        return tTranMapper.selectTranDetail(id);
    }

    @Override
    @Transactional
    public int saveTran(TranQuery tranQuery) {
        TTran tTran = new TTran();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        tTran.setTranNo(sdf.format(new Date()));
        tTran.setCustomerId(tranQuery.getCustomerId());
        tTran.setMoney(tranQuery.getMoney());
        tTran.setExpectedDate(tranQuery.getExpectedDate());
        tTran.setStage(tranQuery.getStage());
        tTran.setDescription(tranQuery.getDescription());
        tTran.setNextContactTime(tranQuery.getNextContactTime());
        tTran.setCreateTime(new Date());

        String token = tranQuery.getToken();
        if (token != null && !token.isEmpty()) {
            tTran.setCreateBy(JWTUtils.parseUserFromJWT(token).getId());
        }

        tTranMapper.insertSelective(tTran);

        TTranHistory history = new TTranHistory();
        history.setTranId(tTran.getId());
        history.setStage(tranQuery.getStage());
        history.setMoney(tranQuery.getMoney());
        history.setExpectedDate(tranQuery.getExpectedDate());
        history.setCreateTime(new Date());
        if (token != null && !token.isEmpty()) {
            history.setCreateBy(JWTUtils.parseUserFromJWT(token).getId());
        }
        tTranHistoryMapper.insertSelective(history);

        return 1;
    }

    @Override
    @Transactional
    public int updateTran(TranQuery tranQuery) {
        TTran oldTran = tTranMapper.selectByPrimaryKey(tranQuery.getId());

        TTran tTran = new TTran();
        tTran.setId(tranQuery.getId());
        tTran.setCustomerId(tranQuery.getCustomerId());
        tTran.setMoney(tranQuery.getMoney());
        tTran.setExpectedDate(tranQuery.getExpectedDate());
        tTran.setStage(tranQuery.getStage());
        tTran.setDescription(tranQuery.getDescription());
        tTran.setNextContactTime(tranQuery.getNextContactTime());
        tTran.setEditTime(new Date());

        String token = tranQuery.getToken();
        if (token != null && !token.isEmpty()) {
            tTran.setEditBy(JWTUtils.parseUserFromJWT(token).getId());
        }

        int result = tTranMapper.updateByPrimaryKeySelective(tTran);

        if (oldTran != null && !oldTran.getStage().equals(tranQuery.getStage())) {
            TTranHistory history = new TTranHistory();
            history.setTranId(tranQuery.getId());
            history.setStage(tranQuery.getStage());
            history.setMoney(tranQuery.getMoney());
            history.setExpectedDate(tranQuery.getExpectedDate());
            history.setCreateTime(new Date());
            if (token != null && !token.isEmpty()) {
                history.setCreateBy(JWTUtils.parseUserFromJWT(token).getId());
            }
            tTranHistoryMapper.insertSelective(history);
        }

        return result;
    }

    @Override
    @Transactional
    public int deleteTranById(Integer id) {
        // 先删除子表数据（外键约束）
        tTranHistoryMapper.deleteByTranId(id);
        tTranRemarkMapper.deleteByTranId(id);
        // 再删除交易本身
        return tTranMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int batchDeleteTrans(List<String> idList) {
        int count = 0;
        for (String id : idList) {
            Integer tranId = Integer.parseInt(id);
            tTranHistoryMapper.deleteByTranId(tranId);
            tTranRemarkMapper.deleteByTranId(tranId);
            count += tTranMapper.deleteByPrimaryKey(tranId);
        }
        return count;
    }
}