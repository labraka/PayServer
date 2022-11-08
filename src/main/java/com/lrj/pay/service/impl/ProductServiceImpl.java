package com.lrj.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrj.pay.dto.QueryReqDto;
import com.lrj.pay.entity.Customer;
import com.lrj.pay.entity.Product;
import com.lrj.pay.enums.ApiResponseEnum;
import com.lrj.pay.enums.ConsumeTypeEnum;
import com.lrj.pay.mapper.ProductMapper;
import com.lrj.pay.response.ApiResponse;
import com.lrj.pay.service.ProductService;
import com.lrj.pay.utils.DateUtil;
import com.lrj.pay.vo.PriceRespVo;
import com.lrj.pay.vo.ProductsRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品表 服务实现类
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final int MONTH_DAYS = 30;

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

    @Override
    public ApiResponse price(QueryReqDto queryReqDto, Customer customer) {
        Product product = getById(queryReqDto.getProductId());
        if (ObjectUtils.isEmpty(product)){
            log.error("产品不存在：{}", queryReqDto.getProductId());
            return ApiResponse.returnFail(ApiResponseEnum.PRODUCT_NONE);
        }

        /*
         * 计算剩余有效天数,会员有效期购买和过有效期（or首次）购买计算规则不一样
         * 1.根据类型判断是购买还是续费
         * 2.根据用户判断属于新购还是新增并发数
         */
        double timeNums;
        Integer num;
        boolean isAdd = false;
        LocalDateTime startTime = LocalDateTime.now();
        if (queryReqDto.getConsumeType() == ConsumeTypeEnum.BUY.getType()){
            num = queryReqDto.getNum();
            if (customer.getLevel() == 1
                    || (!ObjectUtils.isEmpty(customer.getVipEndTime())
                    && customer.getVipEndTime().isBefore(LocalDateTime.now()))) {
                timeNums = queryReqDto.getTimeNum();
            } else {
                LocalDateTime now = LocalDateTime.now();
                long days = DateUtil.differTimeNums(now, customer.getVipEndTime(), 4);
                if (days < 0) {
                    log.error("时间参数错误：{}", days);
                    return ApiResponse.returnFail(ApiResponseEnum.PARAMETER_INVALID);
                }
                days = days + 1;
                timeNums = Math.round(days * 10000 / MONTH_DAYS) / 10000.0;
                isAdd = true;
            }
        }else {
            timeNums = queryReqDto.getTimeNum();
            num = customer.getMasterControlNum();
            if (customer.getVipEndTime().isBefore(LocalDateTime.now())){
                startTime = LocalDateTime.now();
            }else {
                startTime = customer.getVipEndTime();
            }

        }

        //计算价格
        BigDecimal total = product.getPrice()
                .multiply(BigDecimal.valueOf(num))
                .multiply(new BigDecimal(timeNums)).setScale(2, RoundingMode.HALF_UP);
        if (total.compareTo(BigDecimal.ZERO) == 0){
            total = new BigDecimal(0.01);
        }
        //计算到期时间
        LocalDateTime endTime;
        if (isAdd){
            endTime = customer.getVipEndTime();
        }else {
            endTime = DateUtil.addTime(startTime, product.getUnit(), new Double(timeNums).longValue());
        }
        PriceRespVo priceRespVo = new PriceRespVo();
        priceRespVo.setProductId(queryReqDto.getProductId());
        priceRespVo.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        priceRespVo.setEndTime(endTime);
        Map<String, Object> map = new HashMap<>();
        map.put("priceRespVo", priceRespVo);
        map.put("timeNums", timeNums);
        map.put("product", product);
        return ApiResponse.returnSuccess(map);
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
