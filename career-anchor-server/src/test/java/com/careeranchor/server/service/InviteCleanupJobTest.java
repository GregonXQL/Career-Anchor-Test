package com.careeranchor.server.service;

import com.careeranchor.server.mapper.InviteCodeMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InviteCleanupJobTest {
    @Test
    void archivesInvitesOnlyAfterTheTwentyFourHourRetentionWindow() {
        InviteCodeMapper mapper = mock(InviteCodeMapper.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-24T04:00:00Z"), ZoneId.of("Asia/Shanghai"));
        InviteCleanupJob job = new InviteCleanupJob(mapper, clock);

        job.archiveOldInvites();

        verify(mapper).archiveExpiredAndExhausted(LocalDateTime.of(2026, 7, 23, 12, 0));
    }
}
