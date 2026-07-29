package com.bjpowernode.scoring.rules;

import com.bjpowernode.scoring.ScoringRule;
import com.bjpowernode.scoring.ScoringResult;
import com.bjpowernode.scoring.ScoringContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间衰减评分规则
 * 线索创建时间越久，分数越低
 */
@Component
public class TimeDecayScoringRule implements ScoringRule {
    
    @Override
    public String getRuleName() {
        return "时间衰减评分";
    }
    
    @Override
    public String getRuleType() {
        return "TIME_DECAY";
    }
    
    @Override
    public double getDefaultWeight() {
        return 0.2;
    }
    
    @Override
    public ScoringResult evaluate(ScoringContext context) {
        long daysSinceCreation = context.getDaysSinceCreation();
        Map<String, Object> details = new HashMap<>();
        
        double score;
        if (daysSinceCreation <= 30) {
            score = 100;
            details.put("创建时间", daysSinceCreation + "天（30天内，满分）");
        } else if (daysSinceCreation <= 60) {
            score = 80;
            details.put("创建时间", daysSinceCreation + "天（31-60天，80%）");
        } else if (daysSinceCreation <= 90) {
            score = 60;
            details.put("创建时间", daysSinceCreation + "天（61-90天，60%）");
        } else if (daysSinceCreation <= 180) {
            score = 40;
            details.put("创建时间", daysSinceCreation + "天（91-180天，40%）");
        } else {
            score = 20;
            details.put("创建时间", daysSinceCreation + "天（180天以上，20%）");
        }
        
        ScoringResult result = new ScoringResult(getRuleName(), score, getDefaultWeight());
        result.setDetails(details);
        return result;
    }
}
