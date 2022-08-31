package com.lrj.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrj.pay.entity.Product;
import com.lrj.pay.mapper.ProductMapper;
import com.lrj.pay.response.ApiResponse;
import com.lrj.pay.service.ProductService;
import com.lrj.pay.vo.ProductsRespVo;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 产品表 服务实现类
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Product getProduct(Long productId) {
        LambdaQueryWrapper<Product> pqw = new LambdaQueryWrapper<>();
        pqw.eq(Product::getId, productId);
        pqw.eq(Product::getIsDelete, 0);
        Product Product = getOne(pqw);
        return Product;
    }

    @Override
    public ApiResponse findProducts(Long id) {

        if (!ObjectUtils.isEmpty(id)) {
            return ApiResponse.returnSuccess(getProductById(id));
        }
        List<Product> Products = list();
        List<ProductsRespVo> productsRespVos = new ArrayList<>();
        if (CollectionUtils.isEmpty(Products)) {
            return ApiResponse.returnSuccess(productsRespVos);
        }
        BeanCopier copier = BeanCopier.create(Product.class, ProductsRespVo.class, false);
        for (Product Product : Products) {
            ProductsRespVo productsRespVo = new ProductsRespVo();
            copier.copy(Product, productsRespVo, null);
            productsRespVos.add(productsRespVo);
        }
        return ApiResponse.returnSuccess(productsRespVos);
    }


    private ProductsRespVo getProductById(Long id) {
        Product Product = getById(id);
        if (ObjectUtils.isEmpty(Product)) {
            return null;
        }
        ProductsRespVo productsRespVo = new ProductsRespVo();
        BeanCopier copier = BeanCopier.create(Product.class, ProductsRespVo.class, false);
        copier.copy(Product, productsRespVo, null);
        return productsRespVo;
    }
}
