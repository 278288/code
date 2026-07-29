package com.bjpowernode.service;

import com.bjpowernode.mapper.TClueMapper;
import com.bjpowernode.mapper.TClueRemarkMapper;
import com.bjpowernode.mapper.TProductMapper;
import com.bjpowernode.model.TClue;
import com.bjpowernode.model.TClueRemark;
import com.bjpowernode.model.TProduct;
import com.bjpowernode.scoring.LeadScoringEngine;
import com.bjpowernode.scoring.ScoringContext;
import com.bjpowernode.scoring.ScoringReport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 线索评分服务
 * 负责触发评分并更新线索的评分字段
 */
@Service
public class ClueScoringService {

    @Resource
    private LeadScoringEngine scoringEngine;

    @Resource
    private TClueMapper tClueMapper;

    @Resource
    private TClueRemarkMapper tClueRemarkMapper;

    @Resource
    private TProductMapper tProductMapper;

    /**
     * 对线索进行评分并更新数据库
     * @param clueId 线索ID
     * @return 评分报告
     */
    public ScoringReport scoreClue(Integer clueId) {
        // 查询线索详情
        TClue clue = tClueMapper.selectDetailById(clueId);
        if (clue == null) {
            throw new RuntimeException("线索不存在：" + clueId);
        }

        // 查询跟踪记录数量
        int remarkCount = tClueRemarkMapper.countByClueId(clueId);

        // 计算创建天数
        long daysSinceCreation = 0;
        if (clue.getCreateTime() != null) {
            daysSinceCreation = ChronoUnit.DAYS.between(
                clue.getCreateTime().toInstant(),
                new Date().toInstant()
            );
        }

        // 计算最近活动天数
        long daysSinceLastActivity = daysSinceCreation;
        TClueRemark latestRemark = tClueRemarkMapper.selectLatestByClueId(clueId);
        if (latestRemark != null && latestRemark.getCreateTime() != null) {
            daysSinceLastActivity = ChronoUnit.DAYS.between(
                latestRemark.getCreateTime().toInstant(),
                new Date().toInstant()
            );
        }

        // 获取最近跟踪记录内容
        String recentRemarkContent = latestRemark != null ? latestRemark.getNoteContent() : null;

        // 查询产品价格
        double productPrice = 0;
        if (clue.getIntentionProduct() != null) {
            TProduct product = tProductMapper.selectByPrimaryKey(clue.getIntentionProduct());
            if (product != null && product.getQuotation() != null) {
                productPrice = product.getQuotation().doubleValue();
            }
        }

        // 构建评分上下文
        ScoringContext context = scoringEngine.buildContext(
            clue, remarkCount, daysSinceCreation,
            daysSinceLastActivity, recentRemarkContent, productPrice
        );

        // 执行评分
        ScoringReport report = scoringEngine.evaluate(context);

        // 更新线索的评分字段
        TClue updateClue = new TClue();
        updateClue.setId(clueId);
        updateClue.setScore(report.getTotalScore());
        updateClue.setScoreLevel(report.getScoreLevel());
        updateClue.setScoreTime(new Date());

        tClueMapper.updateScoreByPrimaryKey(updateClue);

        return report;
    }

    /**
     * 批量重新评分（用于定时任务）
     */
    public void batchRescore(List<Integer> clueIds) {
        for (Integer clueId : clueIds) {
            try {
                scoreClue(clueId);
            } catch (Exception e) {
                System.err.println("评分失败，线索ID：" + clueId + "，错误：" + e.getMessage());
            }
        }
    }
}
