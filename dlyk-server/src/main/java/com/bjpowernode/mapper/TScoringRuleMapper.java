package com.bjpowernode.mapper;

import com.bjpowernode.model.TScoringRule;
import com.bjpowernode.model.TScoringLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TScoringRuleMapper {
    
    /**
     * 查询所有启用的评分规则
     */
    List<TScoringRule> selectEnabledRules();
    
    /**
     * 根据规则类型查询规则
     */
    TScoringRule selectByRuleType(String ruleType);
    
    /**
     * 更新规则权重
     */
    int updateWeight(Integer id, Double weight);
    
    /**
     * 插入评分日志
     */
    int insertLog(TScoringLog log);
    
    /**
     * 查询线索的评分历史
     */
    List<TScoringLog> selectLogsByClueId(Integer clueId);
}
