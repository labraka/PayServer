package com.lrj.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrj.pay.dto.QueryReqDto;
import com.lrj.pay.entity.Customer;
import com.lrj.pay.entity.Product;
import com.lrj.pay.response.ApiResponse;

/**
 * <p>
 * 产品表 服务类
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
public interface ProductService extends IService<Product> {

    Product getProduct(Long productId);

    ApiResponse findProducts(Long id);

    ApiResponse price(QueryReqDto queryReqDto, Customer customer);
}
