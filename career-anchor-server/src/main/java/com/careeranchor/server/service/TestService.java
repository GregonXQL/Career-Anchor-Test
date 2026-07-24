package com.careeranchor.server.service;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.dto.ScoreResult;
import com.careeranchor.server.dto.TestSubmitRequest;
import com.careeranchor.server.entity.InviteCode;
import com.careeranchor.server.entity.InviteCodeUsage;
import com.careeranchor.server.entity.TestResult;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.InviteCodeUsageMapper;
import com.careeranchor.server.mapper.TestResultMapper;
import com.careeranchor.server.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TestService {
    private final InviteService inviteService;
    private final ScoringService scoringService;
    private final SubmissionRateLimiter rateLimiter;
    private final TestResultMapper testResultMapper;
    private final InviteCodeUsageMapper usageMapper;
    private final UserMapper userMapper;
    private final ResultService resultService;
    private final ObjectMapper objectMapper;
    private final int scaleMax;

    public TestService(InviteService inviteService,
                       ScoringService scoringService,
                       SubmissionRateLimiter rateLimiter,
                       TestResultMapper testResultMapper,
                       InviteCodeUsageMapper usageMapper,
                       UserMapper userMapper,
                       ResultService resultService,
                       ObjectMapper objectMapper,
                       AppProperties properties) {
        this.inviteService = inviteService;
        this.scoringService = scoringService;
        this.rateLimiter = rateLimiter;
        this.testResultMapper = testResultMapper;
        this.usageMapper = usageMapper;
        this.userMapper = userMapper;
        this.resultService = resultService;
        this.objectMapper = objectMapper;
        this.scaleMax = properties.assessment().scaleMax();
    }

    @Transactional
    public ReportResponse submit(long userId, TestSubmitRequest request) {
        requireNickname(userId);
        ScoreResult score = scoringService.score(request.answers(), request.boosted(), scaleMax);
        Instant rateMarker = rateLimiter.acquire(userId);
        try {
            InviteCode invite = inviteService.consume(request.inviteCode());
            TestResult result = buildResult(userId, invite.getId(), request, score);
            testResultMapper.insert(result);

            InviteCodeUsage usage = new InviteCodeUsage();
            usage.setInviteCodeId(invite.getId());
            usage.setUserId(userId);
            usageMapper.insert(usage);

            TestResult stored = testResultMapper.selectById(result.getId());
            return resultService.toReport(stored);
        } catch (RuntimeException exception) {
            rateLimiter.release(userId, rateMarker);
            throw exception;
        }
    }

    private void requireNickname(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getNickname() == null || user.getNickname().isBlank()
                || "微信用户".equals(user.getNickname())
                || user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            throw new BizException(ErrorCode.USER_PROFILE_REQUIRED);
        }
    }

    private TestResult buildResult(long userId, long inviteId, TestSubmitRequest request, ScoreResult score) {
        Map<AnchorType, ScoreResult.AnchorScore> scores = score.scores().stream()
                .collect(Collectors.toMap(ScoreResult.AnchorScore::anchor, Function.identity()));
        TestResult result = new TestResult();
        result.setUserId(userId);
        result.setInviteCodeId(inviteId);
        result.setScaleMax(scaleMax);
        result.setAnswers(answersJson(request));
        result.setScoreTechnical(scores.get(AnchorType.TECHNICAL).raw());
        result.setScoreManagerial(scores.get(AnchorType.MANAGERIAL).raw());
        result.setScoreAutonomy(scores.get(AnchorType.AUTONOMY).raw());
        result.setScoreSecurity(scores.get(AnchorType.SECURITY).raw());
        result.setScoreCreativity(scores.get(AnchorType.CREATIVITY).raw());
        result.setScoreService(scores.get(AnchorType.SERVICE).raw());
        result.setScoreChallenge(scores.get(AnchorType.CHALLENGE).raw());
        result.setScoreLifestyle(scores.get(AnchorType.LIFESTYLE).raw());
        result.setTop1(score.top3().get(0));
        result.setTop2(score.top3().get(1));
        result.setTop3(score.top3().get(2));
        return result;
    }

    private String answersJson(TestSubmitRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "items", request.answers(),
                    "boosted", request.boosted()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize answers", exception);
        }
    }
}
