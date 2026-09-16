package com.example.springboot.service;

import java.util.Map;

public interface IAIService {
    String generateDescription(String name, String typeName, String quality);
    Map<String, Object> suggestPrice(Integer typeId, String name);
}
