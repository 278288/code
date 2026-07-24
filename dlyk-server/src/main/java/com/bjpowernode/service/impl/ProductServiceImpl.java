package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TProductMapper;
import com.bjpowernode.model.TProduct;
import com.bjpowernode.query.ProductQuery;
import com.bjpowernode.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private TProductMapper tProductMapper;

    @Override
    public List<TProduct> getAllOnSaleProduct() {
        return tProductMapper.selectAllOnSaleProduct();
    }

    @Override
    public PageInfo<TProduct> getProductByPage(Integer current) {
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        List<TProduct> list = tProductMapper.selectAll();
        return new PageInfo<>(list);
    }

    @Override
    public TProduct getProductById(Integer id) {
        return tProductMapper.selectByPrimaryKey(id);
    }

    @Override
    public int saveProduct(ProductQuery productQuery) {
        TProduct tProduct = new TProduct();
        tProduct.setName(productQuery.getName());
        tProduct.setGuidePriceS(productQuery.getGuidePriceS());
        tProduct.setGuidePriceE(productQuery.getGuidePriceE());
        tProduct.setQuotation(productQuery.getQuotation());
        tProduct.setState(productQuery.getState());
        tProduct.setCreateTime(new Date());
        return tProductMapper.insertSelective(tProduct);
    }

    @Override
    public int updateProduct(ProductQuery productQuery) {
        TProduct tProduct = new TProduct();
        tProduct.setId(productQuery.getId());
        tProduct.setName(productQuery.getName());
        tProduct.setGuidePriceS(productQuery.getGuidePriceS());
        tProduct.setGuidePriceE(productQuery.getGuidePriceE());
        tProduct.setQuotation(productQuery.getQuotation());
        tProduct.setState(productQuery.getState());
        tProduct.setEditTime(new Date());
        return tProductMapper.updateByPrimaryKeySelective(tProduct);
    }

    @Override
    public int deleteProductById(Integer id) {
        return tProductMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int batchDeleteProducts(List<String> idList) {
        int count = 0;
        for (String id : idList) {
            count += tProductMapper.deleteByPrimaryKey(Integer.parseInt(id));
        }
        return count;
    }
}