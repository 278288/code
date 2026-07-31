package com.bjpowernode.scoring.rules;

import com.bjpowernode.scoring.ScoringRule;
import com.bjpowernode.scoring.ScoringResult;
import com.bjpowernode.scoring.ScoringContext;
import com.bjpowernode.model.TClue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础属性评分规则
 * 评分维度：年收入、职业、年龄、贷款需求
 */
@Component
public class AttributeScoringRule implements ScoringRule {

    @Override
    public String getRuleName() {
        return "基础属性评分";
    }

    @Override
    public String getRuleType() {
        return "ATTRIBUTE";
    }

    @Override
    public double getDefaultWeight() {
        return 0.4;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        TClue clue = context.getClue();
        double score = 0;
        Map<String, Object> details = new HashMap<>();

        // 1. 年收入评分（0-20分）
        if (clue.getYearIncome() != null) {
            BigDecimal income = clue.getYearIncome();
            if (income.compareTo(new BigDecimal("500000")) >= 0) {
                score += 20;
                details.put("年收入", income + "（>=50万，+20分）");
            } else if (income.compareTo(new BigDecimal("300000")) >= 0) {
                score += 15;
                details.put("年收入", income + "（>=30万，+15分）");
            } else if (income.compareTo(new BigDecimal("100000")) >= 0) {
                score += 10;
                details.put("年收入", income + "（>=10万，+10分）");
            } else {
                details.put("年收入", income + "（<10万，+0分）");
            }
        }

        // 2. 职业评分（0-15分）
        if (clue.getJob() != null && !clue.getJob().isEmpty()) {
            String job = clue.getJob();
            if (job.contains("金融") || job.contains("IT") || job.contains("互联网")
                || job.contains("医生") || job.contains("律师") || job.contains("企业主")) {
                score += 15;
                details.put("职业", job + "（优质职业，+15分）");
            } else if (job.contains("销售") || job.contains("经理") || job.contains("主管")) {
                score += 10;
                details.put("职业", job + "（良好职业，+10分）");
            } else {
                score += 5;
                details.put("职业", job + "（普通职业，+5分）");
            }
        }

        // 3. 年龄评分（0-10分）
        if (clue.getAge() != null) {
            int age = clue.getAge();
            if (age >= 25 && age <= 45) {
                score += 10;
                details.put("年龄", age + "岁（25-45岁黄金年龄段，+10分）");
            } else if (age >= 46 && age <= 55) {
                score += 7;
                details.put("年龄", age + "岁（46-55岁，+7分）");
            } else if (age >= 18 && age <= 60) {
                score += 3;
                details.put("年龄", age + "岁（有效年龄段，+3分）");
            } else {
                details.put("年龄", age + "岁（非目标年龄段，+0分）");
            }
        }

        // 4. 贷款需求评分（0-10分）
        if (clue.getNeedLoan() != null && clue.getNeedLoan() == 50) {
            score += 10;
            details.put("贷款需求", "不需要贷款（+10分）");
        } else {
            details.put("贷款需求", "需要贷款（+0分）");
        }

        // 归一化到0-100（满分55分 -> 100分）
        double normalizedScore = (score / 55.0) * 100;

        ScoringResult result = new ScoringResult(getRuleName(), normalizedScore, getDefaultWeight());
        result.setDetails(details);
        return result;
    }
}
