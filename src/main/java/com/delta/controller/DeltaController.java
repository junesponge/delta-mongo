package com.delta.controller;

import com.delta.annotation.Collection;
import com.delta.repository.DeltaMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/2/28 12:08 PM
 */
@RestController
public class DeltaController {

    @Autowired
    @Collection("test")
    DeltaMongoRepository deltaMongoRepository;

    @GetMapping("/test")
    public String test() {
        DeltaMongoRepository repo = this.deltaMongoRepository;
        return repo.findAll().toJSONString();
    }
}
