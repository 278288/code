package com.bjpowernode.web;

import com.bjpowernode.model.TClue;
import com.bjpowernode.query.ClueQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.ClueService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 线索管理 Controller。
 *
 * 接口列表：
 *   GET    /api/clues             分页查询（需 clue:list）
 *   POST   /api/importExcel       批量导入 Excel（需 clue:import）
 *   GET    /api/clue/{phone}      校验手机号是否已存在
 *   POST   /api/clue              新增线索（需 clue:add）
 *   GET    /api/clue/detail/{id}  线索详情（需 clue:view）
 *   PUT    /api/clue              编辑线索（需 clue:edit）
 *   DELETE /api/clue/{id}         删除线索（需 clue:delete，当前未实现）
 */
@RestController
public class ClueController {

    @Resource
    private ClueService clueService;

    @PreAuthorize(value = "hasAuthority('clue:list')")
    @GetMapping(value = "/api/clues")
    public R cluePage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) {
            current = 1;
        }
        PageInfo<TClue> pageInfo = clueService.getClueByPage(current);
        return R.OK(pageInfo);
    }

    /** Excel 批量导入线索 */
    @PreAuthorize(value = "hasAuthority('clue:import')")
    @PostMapping(value = "/api/importExcel")
    public R importExcel(MultipartFile file, @RequestHeader(value = "Authorization") String token) throws IOException {
        clueService.importExcel(file.getInputStream(), token);
        return R.OK();
    }

    /** 校验手机号是否已存在（返回 true 表示可用） */
    @GetMapping(value = "/api/clue/{phone}")
    public R checkPhone(@PathVariable(value = "phone") String phone) {
        Boolean check = clueService.checkPhone(phone);
        return check ? R.OK() : R.FAIL();
    }

    @PreAuthorize(value = "hasAuthority('clue:add')")
    @PostMapping(value = "/api/clue")
    public R addClue(@Valid ClueQuery clueQuery, @RequestHeader(value = "Authorization") String token) {
        clueQuery.setToken(token);
        int save = clueService.saveClue(clueQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PreAuthorize(value = "hasAuthority('clue:view')")
    @GetMapping(value = "/api/clue/detail/{id}")
    public R loadClue(@PathVariable(value = "id") Integer id) {
        TClue tClue = clueService.getClueById(id);
        return R.OK(tClue);
    }

    @PreAuthorize(value = "hasAuthority('clue:edit')")
    @PutMapping(value = "/api/clue")
    public R editClue(@Valid ClueQuery clueQuery, @RequestHeader(value = "Authorization") String token) {
        clueQuery.setToken(token);
        int update = clueService.updateClue(clueQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @PreAuthorize(value = "hasAuthority('clue:delete')")
    @DeleteMapping(value = "/api/clue/{id}")
    public R delClue(@PathVariable(value = "id") Integer id) {
        try {
            int del = clueService.deleteClue(id);
            return del >= 1 ? R.OK() : R.FAIL();
        } catch (RuntimeException e) {
            return R.FAIL(e.getMessage());
        }
    }
}