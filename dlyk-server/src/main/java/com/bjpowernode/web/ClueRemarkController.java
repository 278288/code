package com.bjpowernode.web;

import com.bjpowernode.model.TClueRemark;
import com.bjpowernode.query.ClueRemarkQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.ClueRemarkService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 线索跟踪记录 Controller。
 */
@RestController
public class ClueRemarkController {

    @Resource
    private ClueRemarkService clueRemarkService;

    @PostMapping(value = "/api/clue/remark")
    public R addClueRemark(@Valid @RequestBody ClueRemarkQuery clueRemarkQuery,
                           @RequestHeader(value = "Authorization") String token) {
        clueRemarkQuery.setToken(token);
        int save = clueRemarkService.saveClueRemark(clueRemarkQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    /** 分页查询某个线索的所有跟踪记录，clueId 必传 */
    @GetMapping(value = "/api/clue/remark")
    public R clueRemarkPage(@RequestParam(value = "current", required = false) Integer current,
                            @RequestParam(value = "clueId") Integer clueId) {
        ClueRemarkQuery clueRemarkQuery = new ClueRemarkQuery();
        clueRemarkQuery.setClueId(clueId);

        if (current == null) {
            current = 1;
        }
        PageInfo<TClueRemark> pageInfo = clueRemarkService.getClueRemarkByPage(current, clueRemarkQuery);
        return R.OK(pageInfo);
    }
}