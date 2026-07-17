package com.careeranchor.server.controller;

import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.QuestionsResponse;
import com.careeranchor.server.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ApiResponse<QuestionsResponse> questions() {
        return ApiResponse.ok(questionService.getEnabledQuestions());
    }
}
