package com.careeranchor.server.service;

import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.InviteCodeUsageMapper;
import com.careeranchor.server.mapper.TestResultMapper;
import com.careeranchor.server.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestServiceTest {
    @Test
    void rejectsAnonymousResultsBeforeConsumingAnInviteCode() {
        UserMapper userMapper = mock(UserMapper.class);
        User user = new User();
        user.setId(7L);
        when(userMapper.selectById(7L)).thenReturn(user);
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", Duration.ofDays(7), Duration.ofHours(2)),
                new AppProperties.Wechat("", "", true, false, "https://api.weixin.qq.com", "develop", false),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));
        TestService service = new TestService(
                mock(InviteService.class),
                mock(ScoringService.class),
                mock(SubmissionRateLimiter.class),
                mock(TestResultMapper.class),
                mock(InviteCodeUsageMapper.class),
                userMapper,
                mock(ResultService.class),
                new ObjectMapper(),
                properties);

        assertThatThrownBy(() -> service.submit(7L, null))
                .isInstanceOf(BizException.class)
                .extracting(exception -> ((BizException) exception).errorCode())
                .isEqualTo(ErrorCode.USER_PROFILE_REQUIRED);
    }
}
