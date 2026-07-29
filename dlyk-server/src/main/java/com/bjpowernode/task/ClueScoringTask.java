package com.bjpowernode.task;

import com.bjpowernode.mapper.TClueMapper;
import com.bjpowernode.service.ClueScoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 定时任务：线索评分衰减重算
 * 每天凌晨2点执行，重新计算所有线索的时间衰减评分
 */
@Component
public class ClueScoringTask {
    
    @Resource
    private TClueMapper tClueMapper;
    
    @Resource
    private ClueScoringService clueScoringService;
    
    /**
     * 每天凌晨2点执行评分重算
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void rescoreAllClues() {
        System.out.println("开始执行线索评分衰减重算任务...");
        long startTime = System.currentTimeMillis();
        
        try {
            // 查询所有线索ID
            List<Integer> clueIds = tClueMapper.selectAllIds();
            
            if (clueIds == null || clueIds.isEmpty()) {
                System.out.println("没有找到需要重算的线索");
                return;
            }
            
            System.out.println("共找到 " + clueIds.size() + " 条线索需要重算");
            
            // 批量重新评分
            clueScoringService.batchRescore(clueIds);
            
            long endTime = System.currentTimeMillis();
            System.out.println("线索评分衰减重算任务完成，耗时：" + (endTime - startTime) + "ms");
            
        } catch (Exception e) {
            System.err.println("线索评分衰减重算任务执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
