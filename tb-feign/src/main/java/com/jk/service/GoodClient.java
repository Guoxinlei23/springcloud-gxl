package com.jk.service;

import com.jk.Good;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务接口定义
 *
 * @author 恒宇少年
 */
@FeignClient(name = "good-service")
@RequestMapping(value = "/good")
public interface GoodClient {

    @GetMapping
    Good findById(@RequestParam("goodId") Integer goodId);


    @PostMapping
    void reduceStock(@RequestParam("goodId") Integer goodId, @RequestParam("stock") int stock);
}
