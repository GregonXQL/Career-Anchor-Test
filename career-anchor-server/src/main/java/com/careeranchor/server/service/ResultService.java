package com.careeranchor.server.service;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.dto.PageResponse;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.dto.ResultSummary;
import com.careeranchor.server.dto.ScoreResult;
import com.careeranchor.server.entity.TestResult;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.TestResultMapper;
import com.careeranchor.server.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ResultService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TestResultMapper testResultMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public ResultService(TestResultMapper testResultMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.testResultMapper = testResultMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    public PageResponse<ResultSummary> history(long userId, int page, int size) {
        long total = testResultMapper.countByUserId(userId);
        long offset = (long) (page - 1) * size;
        List<ResultSummary> records = testResultMapper.selectHistory(userId, offset, size).stream()
                .map(result -> new ResultSummary(result.getId(), format(result),
                        result.getTop1(), result.getTop2(), result.getTop3()))
                .toList();
        return new PageResponse<>(total, page, size, records);
    }

    public ReportResponse get(long resultId, AuthContext.Principal principal) {
        TestResult result = testResultMapper.selectById(resultId);
        if (result == null || (principal.role() != Role.ADMIN && result.getUserId() != principal.userId())) {
            throw new BizException(ErrorCode.RESULT_NOT_FOUND);
        }
        return toReport(result);
    }

    public ReportResponse toReport(TestResult result) {
        Map<AnchorType, Integer> raw = rawScores(result);
        List<ScoreResult.AnchorScore> scores = new ArrayList<>();
        raw.forEach((anchor, value) -> {
            double average = value / (double) ScoringService.QUESTIONS_PER_ANCHOR;
            int percent = (int) Math.min(100L, Math.round(value /
                    (double) (ScoringService.QUESTIONS_PER_ANCHOR * result.getScaleMax()) * 100));
            scores.add(new ScoreResult.AnchorScore(anchor, anchor.nameCn(), value, average, percent));
        });
        scores.sort(Comparator.comparingInt(ScoreResult.AnchorScore::raw).reversed()
                .thenComparingInt(score -> score.anchor().priority()));
        User user = userMapper.selectById(result.getUserId());
        ReportResponse.UserView userView = user == null ? null
                : new ReportResponse.UserView(user.getId(), user.getNickname(), user.getAvatarUrl());
        return new ReportResponse(result.getId(), format(result), result.getScaleMax(),
                boosted(result.getAnswers()), List.copyOf(scores),
                List.of(result.getTop1(), result.getTop2(), result.getTop3()), userView);
    }

    private Map<AnchorType, Integer> rawScores(TestResult result) {
        Map<AnchorType, Integer> raw = new EnumMap<>(AnchorType.class);
        raw.put(AnchorType.TECHNICAL, result.getScoreTechnical());
        raw.put(AnchorType.MANAGERIAL, result.getScoreManagerial());
        raw.put(AnchorType.AUTONOMY, result.getScoreAutonomy());
        raw.put(AnchorType.SECURITY, result.getScoreSecurity());
        raw.put(AnchorType.CREATIVITY, result.getScoreCreativity());
        raw.put(AnchorType.SERVICE, result.getScoreService());
        raw.put(AnchorType.CHALLENGE, result.getScoreChallenge());
        raw.put(AnchorType.LIFESTYLE, result.getScoreLifestyle());
        return raw;
    }

    private List<Integer> boosted(String answersJson) {
        try {
            JsonNode boosted = objectMapper.readTree(answersJson).path("boosted");
            if (!boosted.isArray()) {
                throw new JsonProcessingException("boosted is not an array") {};
            }
            List<Integer> ids = new ArrayList<>();
            boosted.forEach(node -> ids.add(node.asInt()));
            return List.copyOf(ids);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored answers JSON is invalid", exception);
        }
    }

    private String format(TestResult result) {
        return result.getCreatedAt().format(DATE_TIME);
    }
}
