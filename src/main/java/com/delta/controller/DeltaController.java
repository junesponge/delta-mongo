package com.delta.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delta.annotation.Collection;
import com.delta.repository.DeltaMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Map;

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
        return this.deltaMongoRepository.insert(JSONObject.parseObject(json)).toJSONString();
    }

    @PutMapping("/update")
    @ResponseBody
    public String update(String json) {
        this.deltaMongoRepository.update(JSONObject.parseObject(json));
        return new JSONObject().fluentPut("result", "success").toJSONString();
    }

    @GetMapping("/findAll")
    public String findAll(Model model) {
        model.addAttribute("result", this.deltaMongoRepository.findAll().toJSONString());
        return "index";
    }

    @GetMapping("/findById")
    @ResponseBody
    public String findById(String id, Date date) {
        JSONObject result = null;
        if (null != date) {
            result = this.deltaMongoRepository.findByIdAndDate(id, date);
        } else {
            result = this.deltaMongoRepository.findById(id);
        }
        return result.toJSONString();
    }

    @GetMapping("/findByQuery")
    @ResponseBody
    public String findByQuery(String queryJson, Date date) {
        Query query = new Query();
        JSONObject jsonObject = JSONObject.parseObject(queryJson);
        for (Map.Entry<String, Object> e : jsonObject.entrySet()) {
            query.addCriteria(Criteria.where(e.getKey()).is(e.getValue()));
        }

        JSONArray result = null;
        if (null != date) {
            result = this.deltaMongoRepository.findByQueryAndDate(query, date);
        } else {
            result = this.deltaMongoRepository.findByQuery(query);
        }
        return result.toJSONString();
    }
}
