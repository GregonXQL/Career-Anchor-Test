package com.careeranchor.server.dto;

import com.careeranchor.server.enums.AnchorType;

public record ResultSummary(long id, String createdAt, AnchorType top1, AnchorType top2, AnchorType top3) {
}
