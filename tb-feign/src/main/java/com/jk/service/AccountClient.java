package com.jk.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : gxl
 * @date : 15:01 2020/8/18
 * @user : 86176
 */
@FeignClient(name = "account-service")
@RequestMapping(value = "/account")
public interface AccountClient {


    @PostMapping
    void deduction(@RequestParam("accountId") Integer accountId, @RequestParam("money") Double money);
}
