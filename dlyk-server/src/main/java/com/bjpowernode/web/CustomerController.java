package com.bjpowernode.web;

import com.alibaba.excel.EasyExcel;
import com.bjpowernode.constant.Constants;
import com.bjpowernode.model.TCustomer;
import com.bjpowernode.query.CustomerQuery;
import com.bjpowernode.result.CustomerExcel;
import com.bjpowernode.result.R;
import com.bjpowernode.service.CustomerService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 客户管理 Controller。
 *
 * 接口列表：
 *   POST /api/clue/customer    线索转化为客户
 *   GET  /api/customers        分页查询客户列表
 *   GET  /api/customer/{id}    客户详情
 *   GET  /api/exportExcel      导出客户 Excel（ids 可选，不传导出全部）
 */
@RestController
public class CustomerController {

    @Resource
    private CustomerService customerService;

    /** 线索 → 客户转化（关联市场活动、创建人、数据权限检查） */
    @PostMapping(value = "/api/clue/customer")
    public R convertCustomer(@Valid @RequestBody CustomerQuery customerQuery,
                              @RequestHeader(value = "Authorization") String token) {
        customerQuery.setToken(token);
        Boolean convert = customerService.convertCustomer(customerQuery);
        return convert ? R.OK() : R.FAIL();
    }

    @GetMapping(value = "/api/customers")
    public R cluePage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) {
            current = 1;
        }
        PageInfo<TCustomer> pageInfo = customerService.getCustomerByPage(current);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/customer/{id}")
    public R customerDetail(@PathVariable(value = "id") Integer id) {
        TCustomer tCustomer = customerService.getCustomerById(id);
        return R.OK(tCustomer);
    }

    /**
     * 导出客户 Excel。
     * 设置响应头为文件下载，用 EasyExcel 写入输出流。
     * 传入 ids 只导出指定客户，不传则导出全部。
     */
    @GetMapping(value = "/api/exportExcel")
    public void exportExcel(HttpServletResponse response,
                             @RequestParam(value = "ids", required = false) String ids) throws IOException {

        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition",
                "attachment;filename="
                + URLEncoder.encode(Constants.EXCEL_FILE_NAME + System.currentTimeMillis(), StandardCharsets.UTF_8)
                + ".xlsx");

        List<String> idList = StringUtils.hasText(ids)
                ? Arrays.asList(ids.split(","))
                : new ArrayList<>();
        List<CustomerExcel> dataList = customerService.getCustomerByExcel(idList);

        EasyExcel.write(response.getOutputStream(), CustomerExcel.class)
                .sheet()
                .doWrite(dataList);
    }
}