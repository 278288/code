package com.bjpowernode.web;

import com.bjpowernode.result.NameValue;
import com.bjpowernode.result.R;
import com.bjpowernode.result.SummaryData;
import com.bjpowernode.service.StatisticService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘统计 Controller。
 * 提供首页仪表盘所需的汇总数据、销售漏斗、线索来源饼图三个接口。
 */
@RestController
public class StatisticController {

    @Resource
    private StatisticService statisticService;

    /** 仪表盘汇总数据（线索数、客户数、交易数、总金额） */
    @GetMapping(value = "/api/summary/data")
    public R summaryData() {
        SummaryData summaryData = statisticService.loadSummaryData();
        return R.OK(summaryData);
    }

    /** 销售漏斗数据（线索 → 客户 → 交易 → 成交各阶段数量） */
    @GetMapping(value = "/api/saleFunnel/data")
    public R saleFunnelData() {
        List<NameValue> nameValueList = statisticService.loadSaleFunnelData();
        return R.OK(nameValueList);
    }

    /** 线索来源饼图数据（各来源的线索数量占比） */
    @GetMapping(value = "/api/sourcePie/data")
    public R sourcePieData() {
        List<NameValue> nameValueList = statisticService.loadSourcePieData();
        return R.OK(nameValueList);
    }
}