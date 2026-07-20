package com.careeranchor.server.service;

import com.careeranchor.server.dto.AdminStatsResponse;
import com.careeranchor.server.dto.PageResponse;
import com.careeranchor.server.dto.AdminInviteView;
import com.careeranchor.server.entity.InviteCode;
import com.careeranchor.server.enums.InviteChannel;
import com.careeranchor.server.enums.InviteStatus;
import com.careeranchor.server.mapper.InviteCodeMapper;
import com.careeranchor.server.mapper.TestResultMapper;
import com.careeranchor.server.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServiceTest {
    private final TestResultMapper results = mock(TestResultMapper.class);
    private final UserMapper users = mock(UserMapper.class);
    private final InviteCodeMapper invites = mock(InviteCodeMapper.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T04:00:00Z"), ZoneId.of("Asia/Shanghai"));
    private final AdminService service = new AdminService(results, users, invites,
            mock(InviteCodeGenerator.class), mock(InviteService.class), mock(ResultService.class),
            mock(WxAcodeService.class), clock);

    @Test
    void inviteListDerivesActiveUsedExpiredAndDisabledStatuses() {
        when(invites.selectList(any())).thenReturn(List.of(
                invite(1, InviteStatus.ACTIVE, 0, 1, LocalDateTime.of(2026, 7, 21, 0, 0)),
                invite(2, InviteStatus.ACTIVE, 1, 1, null),
                invite(3, InviteStatus.ACTIVE, 0, 1, LocalDateTime.of(2026, 7, 19, 0, 0)),
                invite(4, InviteStatus.DISABLED, 0, 1, null)));

        PageResponse<AdminInviteView> all = service.invites(1, 10, "ALL");
        PageResponse<AdminInviteView> used = service.invites(1, 10, "USED");

        assertThat(all.records()).extracting(AdminInviteView::status)
                .containsExactly("ACTIVE", "USED", "EXPIRED", "DISABLED");
        assertThat(used.total()).isEqualTo(1);
        assertThat(used.records().getFirst().id()).isEqualTo(2);
    }

    @Test
    void statsCountOnlyCurrentlyUsableInvitesAsActive() {
        when(invites.selectList(any())).thenReturn(List.of(
                invite(1, InviteStatus.ACTIVE, 0, 1, null),
                invite(2, InviteStatus.ACTIVE, 1, 1, null)));
        when(results.selectCount(any())).thenReturn(12L);
        when(results.countCreatedBetween(any(), any())).thenReturn(3L);
        when(users.selectCount(any())).thenReturn(7L);

        AdminStatsResponse stats = service.stats();

        assertThat(stats).isEqualTo(new AdminStatsResponse(12, 3, 7, 1));
    }

    private InviteCode invite(long id, InviteStatus status, int used, int max, LocalDateTime expiresAt) {
        InviteCode invite = new InviteCode();
        invite.setId(id);
        invite.setCode("ABCDEFG" + id);
        invite.setChannel(InviteChannel.MANUAL);
        invite.setStatus(status);
        invite.setUsedCount(used);
        invite.setMaxUses(max);
        invite.setExpiresAt(expiresAt);
        invite.setCreatedAt(LocalDateTime.of(2026, 7, 18, 0, 0));
        return invite;
    }
}
