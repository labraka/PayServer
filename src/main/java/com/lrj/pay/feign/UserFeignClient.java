package com.lrj.pay.feign;

import com.lrj.pay.entity.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ClassName: UserFeignClient
 * @Description: user服务feign客户端
 * @Date: 2022/8/15 12:04
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@FeignClient("user")
public interface UserFeignClient {
    @GetMapping("/api/user/customer/getUserById")
    Customer getCustomerById(@RequestParam Long userId);

    @PostMapping("/api/user/customer/updateUser")
    void updateUser(@RequestBody Map<String, Object> map);
}
