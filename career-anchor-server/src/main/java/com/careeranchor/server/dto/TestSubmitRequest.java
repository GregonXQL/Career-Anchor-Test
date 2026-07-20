package com.careeranchor.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TestSubmitRequest(
        @NotBlank(message = "邀请码不能为空")
        @Pattern(regexp = "[A-Za-z0-9]{8}", message = "邀请码必须为 8 位字母或数字")
        String inviteCode,
        @NotNull(message = "答案不能为空")
        @Size(min = 40, max = 40, message = "必须提交 40 条答案")
        List<@Valid Answer> answers,
        @NotNull(message = "附加题不能为空")
        @Size(min = 3, max = 3, message = "必须选择恰好 3 道附加题")
        List<Integer> boosted) {
}
