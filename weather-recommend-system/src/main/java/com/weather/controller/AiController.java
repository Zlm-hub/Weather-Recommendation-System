package com.weather.controller;

import com.weather.service.AiAssistantService;
import com.weather.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        // 获取当前登录用户名，用于个性化回答
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 获取 AI 回复
        String answer = aiAssistantService.chat(username, question);

        // ✅✅✅ 关键修复：使用两个参数的方法 (msg, data)
        // 第一个参数是提示语，第二个参数才是真正的回复内容
        // 这样前端 res.data 就能拿到 answer 了
        return Result.success("获取成功", answer);
    }
}