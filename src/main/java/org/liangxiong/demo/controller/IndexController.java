package org.liangxiong.demo.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.liangxiong.demo.entity.House;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangxiong
 * @date 2019-10-28 22:13
 * @description 入口控制器
 **/
@Slf4j
@Api(tags = "首页")
@RestController
public class IndexController {

    @ApiOperation("访问首页")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名称", defaultValue = "李白", required = true)})
    @GetMapping(value = "/index")
    public String index(String username) {
        log.info("welcome {} to our home page", username);
        return username;
    }

    @ApiOperation("读取用户数据")
    @ApiImplicitParam(name = "house", value = "房子")
    @PostMapping("/hello")
    public House hello(House house) {
        return house;
    }
}
