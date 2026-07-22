package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.dto.WxLoginResponse;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.UserMapper;
import com.careeranchor.server.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private WxAuthClient wxAuthClient;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        wxAuthClient = mock(WxAuthClient.class);
        UserMapper userMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", Duration.ofDays(7), Duration.ofHours(2)),
                new AppProperties.Wechat("wx-test-app", "secret", false, "https://api.weixin.qq.com", "trial", false),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));

        User user = new User();
        user.setId(7L);
        user.setOpenid("cloud-openid");
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(jwtUtil.issue(7L, com.careeranchor.server.enums.Role.USER)).thenReturn("jwt-token");
        authService = new AuthService(wxAuthClient, userMapper, jwtUtil, properties);
    }

    @Test
    void cloudRunIdentitySkipsCodeToSession() {
        WxLoginResponse response = authService.wxLogin(null, "wx", "wx-test-app", "cloud-openid");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().id()).isEqualTo(7L);
        verify(wxAuthClient, never()).exchangeCodeForOpenid(any());
    }

    @Test
    void nonCloudRequestFallsBackToCodeToSession() {
        when(wxAuthClient.exchangeCodeForOpenid("login-code")).thenReturn("cloud-openid");

        authService.wxLogin("login-code", null, null, null);

        verify(wxAuthClient).exchangeCodeForOpenid("login-code");
    }

    @Test
    void rejectsCloudIdentityFromAnotherMiniProgram() {
        assertThatThrownBy(() -> authService.wxLogin(null, "wx", "another-app", "cloud-openid"))
                .isInstanceOf(BizException.class)
                .hasMessage("微信云托管 AppID 不匹配");
    }

    @Test
    void rejectsIncompleteCloudIdentity() {
        assertThatThrownBy(() -> authService.wxLogin(null, "wx", null, "cloud-openid"))
                .isInstanceOf(BizException.class)
                .hasMessage("微信云托管身份信息不完整");
    }
}
