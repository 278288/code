package com.bjpowernode.scoring;

import java.util.Map;

/**
 * 评分结果
 */
public class ScoringResult {
    private String ruleName;           // 规则名称
    private double rawScore;           // 原始分数（0-100）
    private double weightedScore;      // 加权后分数
    private double weight;             // 权重（0-1）
    private Map<String, Object> details; // 详细评分依据
    
    public ScoringResult() {}
    
    public ScoringResult(String ruleName, double rawScore, double weight) {
        this.ruleName = ruleName;
        this.rawScore = rawScore;
        this.weight = weight;
        this.weightedScore = rawScore * weight;
    }
    
    // Getters and Setters
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public double getRawScore() { return rawScore; }
    public void setRawScore(double rawScore) { this.rawScore = rawScore; }
    
    public double getWeightedScore() { return weightedScore; }
    public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
