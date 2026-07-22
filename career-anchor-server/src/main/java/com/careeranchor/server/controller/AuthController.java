package com.careeranchor.server.controller;

import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.AdminLoginRequest;
import com.careeranchor.server.dto.AdminLoginResponse;
import com.careeranchor.server.dto.WxLoginRequest;
import com.careeranchor.server.dto.WxLoginResponse;
import com.careeranchor.server.service.AuthService;
import com.careeranchor.server.service.AdminAuthService;
import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final AdminAuthService adminAuthService;

    public AuthController(AuthService authService, AdminAuthService adminAuthService) {
        this.authService = authService;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/wx-login")
    public ApiResponse<WxLoginResponse> wxLogin(
            @RequestHeader(value = "X-WX-SOURCE", required = false) String source,
            @RequestHeader(value = "X-WX-APPID", required = false) String appId,
            @RequestHeader(value = "X-WX-OPENID", required = false) String openId,
            @RequestBody(required = false) WxLoginRequest request) {
        String code = request == null ? null : request.code();
        return ApiResponse.ok(authService.wxLogin(code, source, appId, openId));
    }

    @PostMapping("/admin-login")
    public ApiResponse<AdminLoginResponse> adminLogin(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @Valid @RequestBody AdminLoginRequest request) {
        if (principal.role() != Role.USER) throw new BizException(ErrorCode.FORBIDDEN);
        return ApiResponse.ok(adminAuthService.login(principal.userId(), request.password()));
    }
}
