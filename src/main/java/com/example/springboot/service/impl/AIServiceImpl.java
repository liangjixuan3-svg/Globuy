package com.example.springboot.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.entity.Goods;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.service.IAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIServiceImpl implements IAIService {

    @Resource
    private GoodsMapper goodsMapper;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Override
    public String generateDescription(String name, String typeName, String quality) {
        if (name == null || name.isEmpty()) {
            return "请先输入商品名称以生成描述。";
        }

        String prompt = String.format(
            "你是一个二手交易专家。请根据以下信息，为这件商品撰写一段真实、专业且富有吸引力的描述：\n" +
            "1. 商品名称：%s\n" +
            "2. 商品分类：%s\n" +
            "3. 商品成色：%s\n\n" +
            "特别要求：\n" +
            "1. **必须紧扣商品成色**：如果成色是全新，请强调未拆封或未使用；如果成色较旧，请如实描述可能存在的瑕疵或磨损，并强调性价比。\n" +
            "2. 包含商品核心卖点、实际使用感受和交易建议（如支持校内面交）。\n" +
            "3. 语言亲切，符合大学校园社区氛围，不要太生硬。\n" +
            "4. 使用 HTML 标签进行排版（如 <p>, <strong>, <ul>, <li> 等），让描述易于阅读。\n" +
            "只返回生成的 HTML 内容，不要包含任何前缀（如“好的，这是生成的描述”）或后缀。",
            name, typeName, quality
        );

        String response = callDeepSeek(prompt, "你是一个专业的二手交易描述生成助手。");
        
        // 如果调用失败（如余额不足、超时或配置问题），执行优雅降级，返回一个高质量的本地模板
        if (response.startsWith("AI 接口调用失败") || response.startsWith("网络异常") || 
            response.contains("Insufficient Balance") || response.contains("invalid_api_key") ||
            response.contains("配置有效的 DeepSeek API Key")) {
            return String.format(
                "<p>✨ <strong>商品推荐：</strong> 这是一件超值的 <strong>%s</strong>，属于 <strong>%s</strong> 类别。</p>" +
                "<p>💎 <strong>物品详情：</strong> 目前成色为 <strong>%s</strong>。平时使用非常爱惜，功能完全正常，外观保持良好。</p>" +
                "<ul>" +
                "<li>✅ 性能强劲，满足日常所需</li>" +
                "<li>✅ 校园内面交，质量有保证</li>" +
                "<li>✅ 价格公道，诚心转让</li>" +
                "</ul>" +
                "<p>💡 <strong>交易建议：</strong> 欢迎校内学长学姐、学弟学妹咨询，支持当面验货。诚心想要的小伙伴可以私信详谈哦！</p>" +
                "<p style='color: #999; font-size: 12px;'>(注：AI 服务暂时不可用，已自动切换为智能模板生成)</p>",
                name, typeName, quality
            );
        }
        return response;
    }

    @Override
    public Map<String, Object> suggestPrice(Integer typeId, String name) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type_id", typeId);
        List<Goods> goodsList = goodsMapper.selectList(queryWrapper);

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = null;
        BigDecimal max = BigDecimal.ZERO;
        int count = 0;

        for (Goods g : goodsList) {
            if (g.getPrice() != null) {
                BigDecimal p = g.getPrice();
                sum = sum.add(p);
                if (min == null || p.compareTo(min) < 0) min = p;
                if (p.compareTo(max) > 0) max = p;
                count++;
            }
        }

        BigDecimal avg = BigDecimal.ZERO;
        if (count > 0) {
            avg = sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
        }

        String prompt = String.format(
            "请根据以下平台数据为商品进行价格建议：\n" +
            "商品名称：%s\n" +
            "同类商品统计数据：\n" +
            "- 已有商品数量：%d 件\n" +
            "- 平台最低价：%.2f\n" +
            "- 平台最高价：%.2f\n" +
            "- 平台平均价：%.2f\n\n" +
            "请给出：\n" +
            "1. 一个具体的建议售价（数字）。\n" +
            "2. 简短的理由（20字以内）。\n\n" +
            "请严格按照以下 JSON 格式返回，不要有任何其他文字：\n" +
            "{\"suggestedPrice\": 数字, \"reason\": \"理由\"}",
            name, count, min != null ? min.doubleValue() : 0.0, max.doubleValue(), avg.doubleValue()
        );

        String aiResponse = callDeepSeek(prompt, "你是一个精通二手市场行情的价格分析专家。");
        
        try {
            // 清理可能的 Markdown 代码块标记
            String jsonStr = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
            result.put("suggestedPrice", jsonObject.getBigDecimal("suggestedPrice"));
            result.put("reason", jsonObject.getStr("reason") + " (基于AI分析)");
        } catch (Exception e) {
            // AI 返回失败时回退到基础逻辑
            BigDecimal suggested = avg.multiply(new BigDecimal("0.9")).setScale(2, RoundingMode.HALF_UP);
            result.put("suggestedPrice", suggested.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("50.00") : suggested);
            result.put("reason", count > 0 ? "基于同类商品平均价 9 折推荐。" : "暂无参考数据，建议自行定价。");
        }

        result.put("avgPrice", avg);
        result.put("minPrice", min);
        result.put("maxPrice", max);
        
        return result;
    }

    private String callDeepSeek(String prompt, String systemPrompt) {
        if ("your-api-key-here".equals(apiKey)) {
            return "请在后端 application.yaml 中配置有效的 DeepSeek API Key。";
        }

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
        messages.add(new JSONObject().set("role", "user").set("content", prompt));
        
        requestBody.set("messages", messages);
        requestBody.set("stream", false);

        try {
            HttpResponse response = HttpRequest.post(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(60000) // 设置 60 秒超时
                    .execute();

            if (response.isOk()) {
                JSONObject resJson = JSONUtil.parseObj(response.body());
                return resJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            } else {
                return "AI 接口调用失败: " + response.body();
            }
        } catch (Exception e) {
            return "网络异常，请稍后再试: " + e.getMessage();
        }
    }
}
