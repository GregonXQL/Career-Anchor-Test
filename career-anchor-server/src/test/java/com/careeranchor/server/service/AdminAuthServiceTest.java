package com.careeranchor.server.service;

import com.careeranchor.server.dto.AdminLoginResponse;
import com.careeranchor.server.entity.AdminAccount;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.AdminAccountMapper;
import com.careeranchor.server.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminAuthServiceTest {
    private final AdminAccountMapper mapper = mock(AdminAccountMapper.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AdminLoginGuard guard = new AdminLoginGuard(Clock.fixed(
            Instant.parse("2026-07-20T00:00:00Z"), ZoneId.of("Asia/Shanghai")));
    private final AdminAuthService service = new AdminAuthService(mapper, encoder, guard, jwtUtil);

    @Test
    void issuesAnAdminTokenAfterPasswordVerification() {
        AdminAccount account = account();
        when(mapper.selectOne(any())).thenReturn(account);
        when(encoder.matches("correct", "hash")).thenReturn(true);
        when(jwtUtil.issue(9L, Role.ADMIN)).thenReturn("admin-jwt");

        AdminLoginResponse response = service.login(42L, "correct");

        assertThat(response.adminToken()).isEqualTo("admin-jwt");
    }

    @Test
    void locksTheUserOnTheFifthConsecutiveWrongPassword() {
        when(mapper.selectOne(any())).thenReturn(account());
        when(encoder.matches(any(), any())).thenReturn(false);

        for (int index = 0; index < 4; index += 1) {
            assertThatThrownBy(() -> service.login(42L, "wrong"))
                    .isInstanceOfSatisfying(BizException.class,
                            exception -> assertThat(exception.errorCode()).isEqualTo(ErrorCode.ADMIN_PASSWORD_INVALID));
        }
        assertThatThrownBy(() -> service.login(42L, "wrong"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(ErrorCode.ADMIN_LOGIN_LOCKED));
    }

    private AdminAccount account() {
        AdminAccount account = new AdminAccount();
        account.setId(9L);
        account.setPasswordHash("hash");
        return account;
    }
}
