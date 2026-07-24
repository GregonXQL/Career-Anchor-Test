package com.careeranchor.server.service;

import com.careeranchor.server.mapper.InviteCodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@Lazy(false)
public class InviteCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(InviteCleanupJob.class);
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    private static final long START_DELAY_MS = 60 * 1000L;

    private final InviteCodeMapper inviteCodeMapper;
    private final Clock clock;

    public InviteCleanupJob(InviteCodeMapper inviteCodeMapper, Clock clock) {
        this.inviteCodeMapper = inviteCodeMapper;
        this.clock = clock;
    }

    @Scheduled(initialDelay = START_DELAY_MS, fixedDelay = ONE_HOUR_MS)
    public void archiveOldInvites() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(1);
        int archived = inviteCodeMapper.archiveExpiredAndExhausted(cutoff);
        if (archived > 0) {
            log.info("Archived {} expired or exhausted invite codes", archived);
        }
    }
}
