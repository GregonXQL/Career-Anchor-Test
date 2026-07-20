package com.careeranchor.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Answer(
        @Min(value = 1, message = "题号最小为 1") @Max(value = 40, message = "题号最大为 40") int q,
        @Min(value = 1, message = "分值最小为 1") @Max(value = 6, message = "分值最大为 6") int v) {
}
