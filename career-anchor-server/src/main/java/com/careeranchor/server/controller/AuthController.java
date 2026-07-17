package com.careeranchor.server.controller;

import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.WxLoginRequest;
import com.careeranchor.server.dto.WxLoginResponse;
import com.careeranchor.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wx-login")
    public ApiResponse<WxLoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(authService.wxLogin(request.code()));
    }
}
