package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.IAIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private IAIService aiService;

    @GetMapping("/generateDescription")
    public Result generateDescription(@RequestParam String name, @RequestParam String typeName, @RequestParam String quality) {
        String description = aiService.generateDescription(name, typeName, quality);
        return Result.success(description);
    }

    @GetMapping("/suggestPrice")
    public Result suggestPrice(@RequestParam Integer typeId, @RequestParam String name) {
        Map<String, Object> suggestion = aiService.suggestPrice(typeId, name);
        return Result.success(suggestion);
    }
}
