package com.lingyumin.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [简要描述]:
 * [详细描述]:
 *
 * @author:
 * @date: 5:38 PM 2019/10/12
 * @since: JDK 1.8
 */
@RestController
public class HelloController {

    @RequestMapping("hello")
    public String hello(){
        return "hello spring security";
    }

}
