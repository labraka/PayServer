package com.lrj.pay.controller;


import com.lrj.pay.response.ApiResponse;
import com.lrj.pay.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 产品表 前端控制器
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @GetMapping("/list")
    private ApiResponse findProducts(@RequestParam(value = "id", required = false) Long id){
        return productService.findProducts(id);
    }
}
