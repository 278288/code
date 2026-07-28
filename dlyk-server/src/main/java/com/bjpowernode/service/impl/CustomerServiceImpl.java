package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.manager.CustomerManager;
import com.bjpowernode.mapper.TCustomerMapper;
import com.bjpowernode.model.TCustomer;
import com.bjpowernode.query.CustomerQuery;
import com.bjpowernode.result.CustomerExcel;
import com.bjpowernode.service.CustomerService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 客户管理服务实现。
 *
 * 客户由线索转化而来，核心转换逻辑委托给 CustomerManager（含 @DataScope 权限过滤）。
 * 导出 Excel 时需要将关联对象（负责人、创建人、称呼等）的名称拼接到 CustomerExcel 中。
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerManager customerManager;

    @Resource
    private TCustomerMapper tCustomerMapper;

    /**
     * 线索转化为客户。委托 CustomerManager 处理转换逻辑（含数据权限检查）。
     */
    @Override
    public Boolean convertCustomer(CustomerQuery customerQuery) {
        return customerManager.convertCustomer(customerQuery);
    }

    /**
     * 分页查询客户列表。
     */
    @Override
    public PageInfo<TCustomer> getCustomerByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TCustomer> list = tCustomerMapper.selectCustomerPage();
        PageInfo<TCustomer> info = new PageInfo<>(list);
        return info;
    }

    /**
     * 查询客户详情（复用 Excel 导出查询，传入单个 id 获取带所有关联对象的完整信息）。
     */
    @Override
    public TCustomer getCustomerById(Integer id) {
        List<TCustomer> list = tCustomerMapper.selectCustomerByExcel(Arrays.asList(String.valueOf(id)));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 根据 ID 列表导出客户 Excel 数据。
     * 将 TCustomer 关联对象转换为 CustomerExcel 扁平结构，便于 EasyExcel 导出。
     */
    @Override
    public List<CustomerExcel> getCustomerByExcel(List<String> idList) {
        List<CustomerExcel> customerExcelList = new java.util.ArrayList<>();

        List<TCustomer> tCustomerList = tCustomerMapper.selectCustomerByExcel(idList);

        tCustomerList.forEach(tCustomer -> {
            CustomerExcel customerExcel = new CustomerExcel();
            // 逐一设置字段（无法使用 BeanUtils，因为字段名和嵌套路径不同）
            customerExcel.setOwnerName(ObjectUtils.isEmpty(tCustomer.getOwnerDO()) ? Constants.EMPTY : tCustomer.getOwnerDO().getName());
            customerExcel.setActivityName(ObjectUtils.isEmpty(tCustomer.getActivityDO()) ? Constants.EMPTY : tCustomer.getActivityDO().getName());
            customerExcel.setFullName(tCustomer.getClueDO().getFullName());
            customerExcel.setAppellationName(ObjectUtils.isEmpty(tCustomer.getAppellationDO()) ? Constants.EMPTY : tCustomer.getAppellationDO().getTypeValue());
            customerExcel.setPhone(tCustomer.getClueDO().getPhone());
            customerExcel.setWeixin(tCustomer.getClueDO().getWeixin());
            customerExcel.setQq(tCustomer.getClueDO().getQq());
            customerExcel.setEmail(tCustomer.getClueDO().getEmail());
            customerExcel.setAge(tCustomer.getClueDO().getAge());
            customerExcel.setJob(tCustomer.getClueDO().getJob());
            customerExcel.setYearIncome(tCustomer.getClueDO().getYearIncome());
            customerExcel.setAddress(tCustomer.getClueDO().getAddress());
            customerExcel.setNeedLoadName(ObjectUtils.isEmpty(tCustomer.getNeedLoanDO()) ? Constants.EMPTY : tCustomer.getNeedLoanDO().getTypeValue());
            customerExcel.setProductName(ObjectUtils.isEmpty(tCustomer.getIntentionProductDO()) ? Constants.EMPTY : tCustomer.getIntentionProductDO().getName());
            customerExcel.setSourceName(ObjectUtils.isEmpty(tCustomer.getSourceDO()) ? Constants.EMPTY : tCustomer.getSourceDO().getTypeValue());
            customerExcel.setDescription(tCustomer.getDescription());
            customerExcel.setNextContactTime(tCustomer.getNextContactTime());

            customerExcelList.add(customerExcel);
        });
        return customerExcelList;
    }
}