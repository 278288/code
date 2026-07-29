package com.bjpowernode.model;

import lombok.Data;
import java.util.Date;

/**
 * 评分规则配置
 */
@Data
public class TScoringRule {
    private Integer id;
    private String ruleName;
    private String ruleType;
    private String description;
    private Double weight;
    private Integer enabled;
    private Integer sortOrder;
    private String configJson;
    private Date createTime;
    private Date updateTime;
}
