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

/**
 * 交易管理服务实现。
 *
 * 交易与 t_tran_history 存在外键约束，删除或新增交易时需同步操作历史记录表。
 * 交易编号(tranNo)使用 yyyyMMddHHmmssSSS 格式的时间戳生成，保证唯一。
 */
@Service
public class TranServiceImpl implements TranService {

    @Resource
    private TTranMapper tTranMapper;

    @Resource
    private TTranHistoryMapper tTranHistoryMapper;

    @Resource
    private TTranRemarkMapper tTranRemarkMapper;

    /**
     * 分页查询交易列表（关联查询客户名称和手机号）。
     */
    @Override
    public PageInfo<TTran> getTranByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TTran> list = tTranMapper.selectTranPage();
        return new PageInfo<>(list);
    }

    /**
     * 查询交易详情（含客户、线索关联信息）。
     */
    @Override
    public TTran getTranById(Integer id) {
        return tTranMapper.selectTranDetail(id);
    }

    /**
     * 新增交易。
     * 同时插入交易主表和一条初始历史记录，保证数据一致性（@Transactional）。
     */
    @Override
    @Transactional
    public int saveTran(TranQuery tranQuery) {
        TTran tTran = new TTran();
        // 交易编号：时间戳格式，保证唯一性
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        tTran.setTranNo(sdf.format(new Date()));
        tTran.setCustomerId(tranQuery.getCustomerId());
        tTran.setMoney(tranQuery.getMoney());
        tTran.setExpectedDate(tranQuery.getExpectedDate());
        tTran.setStage(tranQuery.getStage());
        tTran.setDescription(tranQuery.getDescription());
        tTran.setNextContactTime(tranQuery.getNextContactTime());
        tTran.setCreateTime(new Date());

        // 从 JWT 中解析当前登录用户作为创建人
        String token = tranQuery.getToken();
        if (token != null && !token.isEmpty()) {
            tTran.setCreateBy(JWTUtils.parseUserFromJWT(token).getId());
        }

        tTranMapper.insertSelective(tTran);

        // 新增交易时同步插入一条初始阶段的历史记录
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

    /**
     * 编辑交易。
     * 如果交易阶段发生了变更，追加一条新的历史记录；否则仅更新交易主表。
     */
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

        // 阶段变更才追加历史记录
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

    /**
     * 删除交易（含历史记录和备注）。
     * 先删子表（t_tran_history、t_tran_remark），再删主表。
     * 如果主表删除返回 0（记录不存在），抛出异常触发事务回滚，避免子表数据已删但主表还在的不一致问题。
     */
    @Override
    @Transactional
    public int deleteTranById(Integer id) {
        tTranHistoryMapper.deleteByTranId(id);
        tTranRemarkMapper.deleteByTranId(id);
        int result = tTranMapper.deleteByPrimaryKey(id);
        if (result < 1)
            throw new RuntimeException("删除交易失败：交易记录不存在，id=" + id);
        return result;
    }

    /**
     * 批量删除交易（逐个删除，每个都附带子表清理）。
     */
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