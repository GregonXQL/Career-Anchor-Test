package com.careeranchor.server.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InviteCreateRequest(
        @Min(1) @Max(100) int count,
        @Future LocalDateTime expiresAt,
        @Size(max = 128) String remark
) {}
