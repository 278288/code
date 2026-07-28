package com.bjpowernode.web;

import com.bjpowernode.model.TProduct;
import com.bjpowernode.query.ProductQuery;
import com.bjpowernode.result.R;
import com.bjpowernode.service.ProductService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping(value = "/api/products")
    public R productPage(@RequestParam(value = "current", required = false) Integer current) {
        if (current == null) { current = 1; }
        PageInfo<TProduct> pageInfo = productService.getProductByPage(current);
        return R.OK(pageInfo);
    }

    @GetMapping(value = "/api/product/{id}")
    public R productDetail(@PathVariable(value = "id") Integer id) {
        TProduct tProduct = productService.getProductById(id);
        return R.OK(tProduct);
    }

    @PostMapping(value = "/api/product")
    public R addProduct(@Valid ProductQuery productQuery) {
        int save = productService.saveProduct(productQuery);
        return save >= 1 ? R.OK() : R.FAIL();
    }

    @PutMapping(value = "/api/product")
    public R editProduct(@Valid ProductQuery productQuery) {
        int update = productService.updateProduct(productQuery);
        return update >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/product/{id}")
    public R delProduct(@PathVariable(value = "id") Integer id) {
        int del = productService.deleteProductById(id);
        return del >= 1 ? R.OK() : R.FAIL();
    }

    @DeleteMapping(value = "/api/product")
    public R batchDelProduct(@RequestParam(value = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        int batchDel = productService.batchDeleteProducts(idList);
        return batchDel >= idList.size() ? R.OK() : R.FAIL();
    }
}