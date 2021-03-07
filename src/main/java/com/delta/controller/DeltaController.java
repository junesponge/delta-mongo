package com.delta.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/2/28 12:08 PM
 */
@RestController
public class DeltaController {

    @GetMapping("/test")
    public String test() {
        return "hello world";
    }
}
