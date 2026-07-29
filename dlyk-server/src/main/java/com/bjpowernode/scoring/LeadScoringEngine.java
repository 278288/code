package com.bjpowernode.scoring;

import com.bjpowernode.model.TClue;
import com.bjpowernode.scoring.rules.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 线索评分引擎
 * 编排所有评分规则，计算加权总分
 */
@Component
public class LeadScoringEngine {

    @Resource
    private AttributeScoringRule attributeScoringRule;

    @Resource
    private BehaviorScoringRule behaviorScoringRule;

    @Resource
    private TimeDecayScoringRule timeDecayScoringRule;

    @Resource
    private BonusScoringRule bonusScoringRule;

    @Resource
    private AiScoringRule aiScoringRule;

    /**
     * 执行完整评分
     * @param context 评分上下文
     * @return 评分报告
     */
    public ScoringReport evaluate(ScoringContext context) {
        ScoringReport report = new ScoringReport();
        report.setClueId(context.getClue().getId());

        List<ScoringResult> results = new ArrayList<>();

        // 1. 基础属性评分（40%）
        results.add(attributeScoringRule.evaluate(context));

        // 2. 行为活跃度评分（30%）
        results.add(behaviorScoringRule.evaluate(context));

        // 3. 时间衰减评分（20%）
        results.add(timeDecayScoringRule.evaluate(context));

        // 4. 加分项评分（10%）
        results.add(bonusScoringRule.evaluate(context));

        // 5. AI语义分析评分（额外参考，不计入总分）
        results.add(aiScoringRule.evaluate(context));

        report.setRuleResults(results);
        report.calculate();

        return report;
    }

    /**
     * 构建评分上下文
     * @param clue 线索
     * @return 评分上下文
     */
    public ScoringContext buildContext(TClue clue, int remarkCount,
                                       long daysSinceCreation,
                                       long daysSinceLastActivity,
                                       String recentRemarkContent,
                                       double productPrice) {
        ScoringContext context = new ScoringContext(clue);
        context.setRemarkCount(remarkCount);
        context.setDaysSinceCreation(daysSinceCreation);
        context.setDaysSinceLastActivity(daysSinceLastActivity);
        context.setRecentRemarkContent(recentRemarkContent);
        context.setProductPrice(productPrice);
        return context;
    }
}
