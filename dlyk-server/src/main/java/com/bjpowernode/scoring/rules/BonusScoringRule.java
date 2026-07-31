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
        if (clue.getSource() != null) {
            int source = clue.getSource();
            //3=车展会，14=汽车之家，39=员工介绍，43=官方网站，45=门店参观
            if (source == 3 || source == 14 || source == 39 || source == 43 || source == 45 ) {
                score += 15;
                details.put("来源", "市场活动（+15分）");
            } else if (source == 2 || source == 23 || source == 33 || source == 44 ) { // 推荐
                score += 10;
                details.put("来源", "推荐（+10分）");
            } else if (source == 16 || source == 17 || source == 25 || source == 36 || source == 22) { // 广告
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
            if (intention == 46) { // 高意向
                score += 20;
                details.put("意向状态", "有意向（+20分）");
            } else if (intention == 48) { // 中意向
                score += 10;
                details.put("意向状态", "意向不明（+10分）");
            } else {
                details.put("意向状态", "无意向（+0分）");
            }
        }

        // 归一化到0-100（满分60分 -> 100分）
        double normalizedScore = (score / 60.0) * 100;

        ScoringResult result = new ScoringResult(getRuleName(), normalizedScore, getDefaultWeight());
        result.setDetails(details);
        return result;
    }
}
