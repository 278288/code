package com.bjpowernode.scoring;

/**
 * 评分规则接口（策略模式）
 */
public interface ScoringRule {
    
    /**
     * 规则名称
     */
    String getRuleName();
    
    /**
     * 规则类型
     */
    String getRuleType();
    
    /**
     * 默认权重
     */
    double getDefaultWeight();
    
    /**
     * 执行评分
     * @param context 评分上下文
     * @return 评分结果（原始分0-100）
     */
    ScoringResult evaluate(ScoringContext context);
}
