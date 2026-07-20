package com.careeranchor.server.controller;

import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.InviteVerifyRequest;
import com.careeranchor.server.dto.InviteVerifyResponse;
import com.careeranchor.server.entity.InviteCode;
import com.careeranchor.server.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invite")
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/verify")
    public ApiResponse<InviteVerifyResponse> verify(@Valid @RequestBody InviteVerifyRequest request) {
        InviteCode invite = inviteService.verify(request.code());
        return ApiResponse.ok(new InviteVerifyResponse(true, invite.getChannel()));
    }
}
