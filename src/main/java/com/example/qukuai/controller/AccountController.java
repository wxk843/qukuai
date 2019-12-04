package com.example.qukuai.controller;

import com.example.qukuai.model.ServiceResponse;
import com.example.qukuai.service.BlockchainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author deray.wang
 * @date 2019/11/27 17:16
 */
@Slf4j
@RestController
@RequestMapping("/account")
@Api(description = "account")
public class AccountController {
    @Autowired
    private BlockchainService blockchainService;

    @RequestMapping(value = "/newAccount", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", value = "创建地址", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse newAccount(@ApiParam(name = "password") @RequestParam(name = "password") String password) {

        String addr = blockchainService.newAccount(password);
        System.out.println(addr);
        return ServiceResponse.createSuccessResponse("",addr);
    }

    @RequestMapping(value = "/getAccount", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", value = "获取钱包里的所有用户", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getAllAccounts() {

        List<String> accounts = blockchainService.getAllAccounts();

        return ServiceResponse.createSuccessResponse("",accounts);
    }
}
