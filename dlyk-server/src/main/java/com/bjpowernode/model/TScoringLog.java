package com.bjpowernode.model;

import lombok.Data;
import java.util.Date;

/**
 * 评分日志
 */
@Data
public class TScoringLog {
    private Integer id;
    private Integer clueId;
    private Integer ruleId;
    private String ruleType;
    private String ruleName;
    private Double score;
    private Double weight;
    private Double weightedScore;
    private String detail;
    private Date createTime;
}
