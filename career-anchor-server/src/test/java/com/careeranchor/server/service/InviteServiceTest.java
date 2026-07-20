package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.careeranchor.server.entity.InviteCode;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.InviteChannel;
import com.careeranchor.server.enums.InviteStatus;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.InviteCodeMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InviteServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-20T02:00:00Z"), ZoneOffset.UTC);
    private final InviteCodeMapper mapper = mock(InviteCodeMapper.class);
    private final InviteService service = new InviteService(mapper, CLOCK);

    @Test
    void verifiesAndNormalizesAnActiveQrInvitation() {
        InviteCode invite = invite(InviteStatus.ACTIVE, 0, 2,
                LocalDateTime.of(2026, 7, 21, 2, 0));
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(invite);

        assertThat(service.verify("  m2qr8888 ")).isSameAs(invite);
        assertThat(service.normalize("  m2qr8888 ")).isEqualTo("M2QR8888");
        assertThat(invite.getChannel()).isEqualTo(InviteChannel.QR);
    }

    @Test
    void returnsSpecificInvitationFailures() {
        when(mapper.selectOne(any(Wrapper.class)))
                .thenReturn(null)
                .thenReturn(invite(InviteStatus.DISABLED, 0, 1, null))
                .thenReturn(invite(InviteStatus.ACTIVE, 0, 1,
                        LocalDateTime.of(2026, 7, 20, 2, 0)))
                .thenReturn(invite(InviteStatus.ACTIVE, 1, 1, null));

        assertError(() -> service.verify("UNKNOWN1"), ErrorCode.INVITE_NOT_FOUND);
        assertError(() -> service.verify("DISABLED"), ErrorCode.INVITE_DISABLED);
        assertError(() -> service.verify("EXPIRED1"), ErrorCode.INVITE_EXPIRED);
        assertError(() -> service.verify("USEDUP11"), ErrorCode.INVITE_EXHAUSTED);
    }

    @Test
    void consumesWithOneAtomicUpdate() {
        InviteCode consumed = invite(InviteStatus.ACTIVE, 1, 2, null);
        when(mapper.consumeAtomically("M2TEST88")).thenReturn(1);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(consumed);

        assertThat(service.consume("m2test88")).isSameAs(consumed);
    }

    private InviteCode invite(InviteStatus status, int used, int max, LocalDateTime expiresAt) {
        InviteCode invite = new InviteCode();
        invite.setId(7L);
        invite.setCode("M2QR8888");
        invite.setStatus(status);
        invite.setChannel(InviteChannel.QR);
        invite.setUsedCount(used);
        invite.setMaxUses(max);
        invite.setExpiresAt(expiresAt);
        return invite;
    }

    private void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(expected));
    }
}
