package com.delta.controller;

import com.alibaba.fastjson.JSONObject;
import com.delta.annotation.Collection;
import com.delta.repository.DeltaMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/2/28 12:08 PM
 */
@Controller
public class DeltaController {

    @Autowired
    @Collection("test")
    DeltaMongoRepository deltaMongoRepository;

    @PostMapping("/create")
    @ResponseBody
    public String create(String json) {
        DeltaMongoRepository repo = this.deltaMongoRepository;
        return repo.insert(JSONObject.parseObject(json)).toJSONString();
    }

    @PutMapping("/update")
    @ResponseBody
    public String update(String json) {
        DeltaMongoRepository repo = this.deltaMongoRepository;
        repo.update(JSONObject.parseObject(json));
        return new JSONObject().fluentPut("result", "success").toJSONString();
    }

    @GetMapping("/findAll")
    public String findAll(Model model) {
        DeltaMongoRepository repo = this.deltaMongoRepository;
        model.addAttribute("result", repo.findAll().toJSONString());
        return "index";
    }

    @GetMapping("/findById")
    @ResponseBody
    public String findById(String id) {
        DeltaMongoRepository repo = this.deltaMongoRepository;
        return repo.findById(id).toJSONString();
    }
}
