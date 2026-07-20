package com.careeranchor.server.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record QrInviteCreateRequest(
        @Future LocalDateTime expiresAt,
        @Min(1) @Max(10000) Integer maxUses,
        @Size(max = 128) String remark
) {}
