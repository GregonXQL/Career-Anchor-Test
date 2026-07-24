package com.careeranchor.server.service;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.entity.TestResult;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.TestResultMapper;
import com.careeranchor.server.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResultServiceTest {
    private final TestResultMapper mapper = mock(TestResultMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final ResultService service = new ResultService(mapper, userMapper, new ObjectMapper());

    @Test
    void buildsAReportAndEnforcesOwnership() {
        TestResult result = result();
        when(mapper.selectById(9L)).thenReturn(result);
        User user = new User();
        user.setId(42L);
        user.setNickname("小林");
        user.setAvatarUrl("data:image/jpeg;base64,aGVsbG8=");
        when(userMapper.selectById(42L)).thenReturn(user);

        ReportResponse report = service.get(9L, new AuthContext.Principal(42L, Role.USER));

        assertThat(report.id()).isEqualTo(9L);
        assertThat(report.createdAt()).isEqualTo("2026-07-20 10:30:00");
        assertThat(report.boosted()).containsExactly(1, 9, 17);
        assertThat(report.top3()).containsExactly(
                AnchorType.TECHNICAL, AnchorType.MANAGERIAL, AnchorType.AUTONOMY);
        assertThat(report.scores()).extracting(score -> score.anchor())
                .containsExactly(AnchorType.TECHNICAL, AnchorType.MANAGERIAL,
                        AnchorType.AUTONOMY, AnchorType.SECURITY, AnchorType.CREATIVITY,
                        AnchorType.SERVICE, AnchorType.CHALLENGE, AnchorType.LIFESTYLE);
        assertThat(report.scores().getFirst().percent()).isEqualTo(100);
        assertThat(report.user().nickname()).isEqualTo("小林");
        assertThat(report.user().avatarUrl()).startsWith("data:image/jpeg");

        assertThatThrownBy(() -> service.get(9L,
                new AuthContext.Principal(99L, Role.USER)))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(ErrorCode.RESULT_NOT_FOUND));
        assertThat(service.get(9L, new AuthContext.Principal(1L, Role.ADMIN)).id()).isEqualTo(9L);
    }

    @Test
    void rejectsCorruptStoredAnswers() {
        TestResult result = result();
        result.setAnswers("{\"boosted\":\"wrong\"}");

        assertThatThrownBy(() -> service.toReport(result))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("answers JSON");
    }

    private TestResult result() {
        TestResult result = new TestResult();
        result.setId(9L);
        result.setUserId(42L);
        result.setScaleMax(6);
        result.setAnswers("{\"items\":[],\"boosted\":[1,9,17]}");
        result.setScoreTechnical(42);
        result.setScoreManagerial(35);
        result.setScoreAutonomy(30);
        result.setScoreSecurity(25);
        result.setScoreCreativity(20);
        result.setScoreService(15);
        result.setScoreChallenge(10);
        result.setScoreLifestyle(5);
        result.setTop1(AnchorType.TECHNICAL);
        result.setTop2(AnchorType.MANAGERIAL);
        result.setTop3(AnchorType.AUTONOMY);
        result.setCreatedAt(LocalDateTime.of(2026, 7, 20, 10, 30));
        return result;
    }
}
