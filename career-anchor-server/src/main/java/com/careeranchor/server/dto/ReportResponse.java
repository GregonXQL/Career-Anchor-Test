package com.careeranchor.server.dto;

import com.careeranchor.server.enums.AnchorType;

import java.util.List;

public record ReportResponse(
        long id,
        String createdAt,
        int scaleMax,
        List<Integer> boosted,
        List<ScoreResult.AnchorScore> scores,
        List<AnchorType> top3,
        UserView user) {
    public record UserView(long id, String nickname, String avatarUrl) {
    }
}
