package com.bjpowernode.web;

import com.bjpowernode.result.R;
import com.bjpowernode.service.ClueScoringService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/clue/scoring")
public class ClueScoringController {
    
    @Resource
    private ClueScoringService clueScoringService;
    
    /**
     * 对指定线索进行评分
     */
    @PostMapping("/{clueId}")
    public R scoreClue(@PathVariable Integer clueId) {
        try {
            return R.OK(clueScoringService.scoreClue(clueId));
        } catch (Exception e) {
            return R.FAIL("评分失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量重新评分
     */
    @PostMapping("/batch")
    public R batchRescore(@RequestBody java.util.List<Integer> clueIds) {
        try {
            clueScoringService.batchRescore(clueIds);
            return R.OK("批量评分完成");
        } catch (Exception e) {
            return R.FAIL("批量评分失败：" + e.getMessage());
        }
    }
}
