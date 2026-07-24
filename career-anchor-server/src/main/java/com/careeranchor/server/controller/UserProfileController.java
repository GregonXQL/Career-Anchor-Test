package com.careeranchor.server.controller;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.UserProfileResponse;
import com.careeranchor.server.dto.UserProfileUpdateRequest;
import com.careeranchor.server.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> get(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal) {
        return ApiResponse.ok(userProfileService.get(principal.userId()));
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> update(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.ok(userProfileService.update(principal.userId(), request));
    }
}
