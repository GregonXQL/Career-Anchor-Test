package com.careeranchor.server.controller;

import com.careeranchor.server.dto.AnchorProfileResponse;
import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.service.AnchorProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/anchor-profiles")
public class AnchorProfileController {
    private final AnchorProfileService anchorProfileService;

    public AnchorProfileController(AnchorProfileService anchorProfileService) {
        this.anchorProfileService = anchorProfileService;
    }

    @GetMapping
    public ApiResponse<List<AnchorProfileResponse>> profiles() {
        return ApiResponse.ok(anchorProfileService.findAll());
    }
}
