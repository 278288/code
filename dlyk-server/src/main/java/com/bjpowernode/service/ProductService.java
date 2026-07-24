package com.bjpowernode.service;

import com.bjpowernode.model.TProduct;
import com.bjpowernode.query.ProductQuery;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface ProductService {

    List<TProduct> getAllOnSaleProduct();

    PageInfo<TProduct> getProductByPage(Integer current);

    TProduct getProductById(Integer id);

    int saveProduct(ProductQuery productQuery);

    int updateProduct(ProductQuery productQuery);

    int deleteProductById(Integer id);

    int batchDeleteProducts(List<String> idList);
}