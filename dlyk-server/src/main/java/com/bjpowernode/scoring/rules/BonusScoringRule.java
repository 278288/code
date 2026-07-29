package com.bjpowernode.scoring.rules;

import com.bjpowernode.scoring.ScoringRule;
import com.bjpowernode.scoring.ScoringResult;
import com.bjpowernode.scoring.ScoringContext;
import com.bjpowernode.model.TClue;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 加分项评分规则
 * 根据来源和产品价值给予额外加分
 */
@Component
public class BonusScoringRule implements ScoringRule {
    
    @Override
    public String getRuleName() {
        return "加分项评分";
    }
    
    @Override
    public String getRuleType() {
        return "BONUS";
    }
    
    @Override
    public double getDefaultWeight() {
        return 0.1;
    }
    
    @Override
    public ScoringResult evaluate(ScoringContext context) {
        TClue clue = context.getClue();
        double score = 0;
        Map<String, Object> details = new HashMap<>();
        
        // 1. 来源加分（0-15分）
        // source: 1=市场活动，2=广告，3=推荐，4=其他
        if (clue.getSource() != null) {
            int source = clue.getSource();
            if (source == 1) { // 市场活动
                score += 15;
                details.put("来源", "市场活动（+15分）");
            } else if (source == 3) { // 推荐
                score += 10;
                details.put("来源", "推荐（+10分）");
            } else if (source == 2) { // 广告
                score += 5;
                details.put("来源", "广告（+5分）");
            } else {
                details.put("来源", "其他（+0分）");
            }
        }
        
        // 2. 产品价值加分（0-25分）
        double productPrice = context.getProductPrice();
        if (productPrice > 0) {
            if (productPrice >= 100000) { // 10万以上
                score += 25;
                details.put("产品价值", "¥" + productPrice + "（>=10万，+25分）");
            } else if (productPrice >= 50000) { // 5万以上
                score += 20;
                details.put("产品价值", "¥" + productPrice + "（>=5万，+20分）");
            } else if (productPrice >= 20000) { // 2万以上
                score += 15;
                details.put("产品价值", "¥" + productPrice + "（>=2万，+15分）");
            } else if (productPrice >= 5000) { // 5千以上
                score += 10;
                details.put("产品价值", "¥" + productPrice + "（>=5千，+10分）");
            } else {
                score += 5;
                details.put("产品价值", "¥" + productPrice + "（<5千，+5分）");
            }
        } else {
            details.put("产品价值", "无（+0分）");
        }
        
        // 3. 意向状态加分（0-20分）
        if (clue.getIntentionState() != null) {
            int intention = clue.getIntentionState();
            if (intention == 3) { // 高意向
                score += 20;
                details.put("意向状态", "高意向（+20分）");
            } else if (intention == 2) { // 中意向
                score += 10;
                details.put("意向状态", "中意向（+10分）");
            } else {
                details.put("意向状态", "低意向（+0分）");
            }
        }
        
        // 归一化到0-100（满分60分 -> 100分）
        double normalizedScore = (score / 60.0) * 100;
        
        ScoringResult result = new ScoringResult(getRuleName(), normalizedScore, getDefaultWeight());
        result.setDetails(details);
        return result;
    }
}
