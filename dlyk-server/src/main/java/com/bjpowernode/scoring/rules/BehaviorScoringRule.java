package com.bjpowernode.scoring.rules;

import com.bjpowernode.scoring.ScoringRule;
import com.bjpowernode.scoring.ScoringResult;
import com.bjpowernode.scoring.ScoringContext;
import com.bjpowernode.model.TClue;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 行为活跃度评分规则
 * 评分维度：跟踪记录数量、最近活跃度、线索状态
 */
@Component
public class BehaviorScoringRule implements ScoringRule {

    @Override
    public String getRuleName() {
        return "行为活跃度评分";
    }

    @Override
    public String getRuleType() {
        return "BEHAVIOR";
    }

    @Override
    public double getDefaultWeight() {
        return 0.3;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        TClue clue = context.getClue();
        double score = 0;
        Map<String, Object> details = new HashMap<>();

        // 1. 跟踪记录数量评分（0-30分）
        // 每条记录5分，最多6条记录
        int remarkCount = context.getRemarkCount();
        int remarkScore = Math.min(remarkCount * 5, 30);
        score += remarkScore;
        details.put("跟踪记录", remarkCount + "条（" + remarkScore + "分）");

        // 2. 最近活跃度评分（0-20分）
        long daysSinceLastActivity = context.getDaysSinceLastActivity();
        if (daysSinceLastActivity <= 3) {
            score += 20;
            details.put("最近活跃", daysSinceLastActivity + "天内（+20分）");
        } else if (daysSinceLastActivity <= 7) {
            score += 15;
            details.put("最近活跃", daysSinceLastActivity + "天内（+15分）");
        } else if (daysSinceLastActivity <= 14) {
            score += 10;
            details.put("最近活跃", daysSinceLastActivity + "天内（+10分）");
        } else if (daysSinceLastActivity <= 30) {
            score += 5;
            details.put("最近活跃", daysSinceLastActivity + "天内（+5分）");
        } else {
            details.put("最近活跃", daysSinceLastActivity + "天未活跃（+0分）");
        }

        // 3. 线索状态评分（0-20分）
        // 状态值：0=新建，1=跟进中，2=已预约，3=已成交，-1=无效
        if (clue.getState() != null) {
            int state = clue.getState();
            if (state == -1) { // 已成交
                score += 20;
                details.put("线索状态", "已转客户（+20分）");
            } else if (state == 27) { // 已预约
                score += 15;
                details.put("线索状态", "已联系（+15分）");
            } else if (state == 10 || state == 6) { // 跟进中
                score += 10;
                details.put("线索状态", "跟进中（+10分）");
            } else if (state == 7 || state == 30 || state == 24) { // 新建
                score += 5;
                details.put("线索状态", "新建（+5分）");
            } else { // 无效
                details.put("线索状态", "无效（+0分）");
            }
        }

        // 归一化到0-100（满分70分 -> 100分）
        double normalizedScore = (score / 70.0) * 100;

        ScoringResult result = new ScoringResult(getRuleName(), normalizedScore, getDefaultWeight());
        result.setDetails(details);
        return result;
    }
}
