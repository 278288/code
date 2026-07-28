package com.bjpowernode.service.impl;

import com.bjpowernode.manager.StatisticManager;
import com.bjpowernode.result.NameValue;
import com.bjpowernode.result.SummaryData;
import com.bjpowernode.service.StatisticService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据统计服务实现。
 * 委托 StatisticManager 计算仪表盘所需的汇总数据和图表数据（销售漏斗、线索来源饼图）。
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    @Resource
    private StatisticManager statisticManager;

    /**
     * 加载仪表盘汇总数据（线索数、客户数、交易数、总金额）。
     */
    @Override
    public SummaryData loadSummaryData() {
        return statisticManager.loadSummaryData();
    }

    /**
     * 销售漏斗数据（线索 → 客户 → 交易 → 成交，各阶段数量）。
     */
    @Override
    public List<NameValue> loadSaleFunnelData() {
        return statisticManager.loadSaleFunnelData();
    }

    /**
     * 线索来源饼图数据（各来源线索数量分布）。
     */
    @Override
    public List<NameValue> loadSourcePieData() {
        return statisticManager.loadSourcePieData();
    }
}