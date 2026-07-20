package com.careeranchor.server.controller;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.dto.TestSubmitRequest;
import com.careeranchor.server.service.TestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tests")
public class TestController {
    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/submit")
    public ApiResponse<ReportResponse> submit(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @Valid @RequestBody TestSubmitRequest request) {
        return ApiResponse.ok(testService.submit(principal.userId(), request));
    }
}
