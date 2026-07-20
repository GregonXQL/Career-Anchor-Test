package com.careeranchor.server.controller;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.PageResponse;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.dto.ResultSummary;
import com.careeranchor.server.service.ResultService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/results")
public class ResultController {
    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ResultSummary>> history(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ApiResponse.ok(resultService.history(principal.userId(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportResponse> detail(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @PathVariable @Min(1) long id) {
        return ApiResponse.ok(resultService.get(id, principal));
    }
}
