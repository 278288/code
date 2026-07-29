package com.bjpowernode.scoring;

import com.bjpowernode.model.TClue;

import java.util.List;

/**
 * 评分上下文 - 传递评分所需的数据
 */
public class ScoringContext {
    
    /** 当前评分的线索 */
    private TClue clue;
    
    /** 线索的跟进记录数量 */
    private int remarkCount;
    
    /** 距离创建的天数 */
    private long daysSinceCreation;
    
    /** 距离最近跟进的天数 */
    private long daysSinceLastActivity;
    
    /** 最近的跟进记录内容 */
    private String recentRemarkContent;
    
    /** 意向产品的价格 */
    private double productPrice;

    public ScoringContext(TClue clue) {
        this.clue = clue;
    }

    public TClue getClue() {
        return clue;
    }

    public void setClue(TClue clue) {
        this.clue = clue;
    }

    public int getRemarkCount() {
        return remarkCount;
    }

    public void setRemarkCount(int remarkCount) {
        this.remarkCount = remarkCount;
    }

    public long getDaysSinceCreation() {
        return daysSinceCreation;
    }

    public void setDaysSinceCreation(long daysSinceCreation) {
        this.daysSinceCreation = daysSinceCreation;
    }

    public long getDaysSinceLastActivity() {
        return daysSinceLastActivity;
    }

    public void setDaysSinceLastActivity(long daysSinceLastActivity) {
        this.daysSinceLastActivity = daysSinceLastActivity;
    }

    public String getRecentRemarkContent() {
        return recentRemarkContent;
    }

    public void setRecentRemarkContent(String recentRemarkContent) {
        this.recentRemarkContent = recentRemarkContent;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }
}
