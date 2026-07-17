package com.careeranchor.server.dto;

import java.util.List;

public record QuestionsResponse(List<QuestionView> questions, AssessmentConfig config) {
    public record QuestionView(int id, String content) {}
    public record AssessmentConfig(int scaleMax, List<String> scaleLabels, int boostCount, int boostValue) {}
}
