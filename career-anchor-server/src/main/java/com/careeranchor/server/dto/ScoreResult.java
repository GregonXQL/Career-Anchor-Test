package com.careeranchor.server.dto;

import com.careeranchor.server.enums.AnchorType;

import java.util.List;

public record ScoreResult(List<AnchorScore> scores, List<AnchorType> top3) {
    public record AnchorScore(AnchorType anchor, String nameCn, int raw, double avg, int percent) {}
}
