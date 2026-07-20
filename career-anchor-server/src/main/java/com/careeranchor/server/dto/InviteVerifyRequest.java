package com.careeranchor.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InviteVerifyRequest(
        @NotBlank(message = "邀请码不能为空")
        @Pattern(regexp = "[A-Za-z0-9]{8}", message = "邀请码必须为 8 位字母或数字")
        String code) {
}
