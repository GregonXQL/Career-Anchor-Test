package com.careeranchor.server.dto;

import com.careeranchor.server.enums.AnchorType;

public record AdminResultSummary(long id, long userId, String nickname, String avatarUrl,
                                 String openidSuffix, String createdAt,
                                 AnchorType top1, AnchorType top2, AnchorType top3) {}
