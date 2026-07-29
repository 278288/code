package com.bjpowernode.scoring;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 评分报告
 */
public class ScoringReport {
    private int clueId;                          // 线索ID
    private double totalScore;                   // 总分（0-100）
    private String scoreLevel;                   // 评分等级：A/B/C/D
    private List<ScoringResult> ruleResults;     // 各规则评分结果
    private Map<String, Object> summary;         // 评分摘要
    
    public ScoringReport() {
        this.summary = new HashMap<>();
    }
    
    /**
     * 计算总分和等级
     */
    public void calculate() {
        if (ruleResults != null && !ruleResults.isEmpty()) {
            // AI评分规则(type=AI)作为参考分，不计入总分
            totalScore = ruleResults.stream()
                    .filter(r -> !r.getRuleName().contains("AI"))
                    .mapToDouble(ScoringResult::getWeightedScore)
                    .sum();
            
            // 限制在0-100范围内
            totalScore = Math.max(0, Math.min(100, totalScore));
            
            // 计算等级
            if (totalScore >= 80) {
                scoreLevel = "A";
            } else if (totalScore >= 60) {
                scoreLevel = "B";
            } else if (totalScore >= 40) {
                scoreLevel = "C";
            } else {
                scoreLevel = "D";
            }
        }
    }
    
    // Getters and Setters
    public int getClueId() { return clueId; }
    public void setClueId(int clueId) { this.clueId = clueId; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    
    public String getScoreLevel() { return scoreLevel; }
    public void setScoreLevel(String scoreLevel) { this.scoreLevel = scoreLevel; }
    
    public List<ScoringResult> getRuleResults() { return ruleResults; }
    public void setRuleResults(List<ScoringResult> ruleResults) { this.ruleResults = ruleResults; }
    
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
}

