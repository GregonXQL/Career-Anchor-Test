package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.entity.InviteCode;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.InviteStatus;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.InviteCodeMapper;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class InviteService {
    private final InviteCodeMapper inviteCodeMapper;
    private final Clock clock;

    public InviteService(InviteCodeMapper inviteCodeMapper, Clock clock) {
        this.inviteCodeMapper = inviteCodeMapper;
        this.clock = clock;
    }

    public InviteCode verify(String rawCode) {
        InviteCode invite = find(rawCode);
        ensureUsable(invite);
        return invite;
    }

    public InviteCode consume(String rawCode) {
        String code = normalize(rawCode);
        if (inviteCodeMapper.consumeAtomically(code) == 1) {
            return find(code);
        }
        InviteCode invite = find(code);
        ensureUsable(invite);
        throw new BizException(ErrorCode.INVITE_EXHAUSTED);
    }

    String normalize(String rawCode) {
        return rawCode == null ? "" : rawCode.strip().toUpperCase(Locale.ROOT);
    }

    private InviteCode find(String rawCode) {
        InviteCode invite = inviteCodeMapper.selectOne(Wrappers.<InviteCode>lambdaQuery()
                .eq(InviteCode::getCode, normalize(rawCode)));
        if (invite == null) {
            throw new BizException(ErrorCode.INVITE_NOT_FOUND);
        }
        return invite;
    }

    private void ensureUsable(InviteCode invite) {
        if (invite.getStatus() == InviteStatus.DISABLED) {
            throw new BizException(ErrorCode.INVITE_DISABLED);
        }
        if (invite.getExpiresAt() != null && !invite.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            throw new BizException(ErrorCode.INVITE_EXPIRED);
        }
        if (invite.getUsedCount() >= invite.getMaxUses()) {
            throw new BizException(ErrorCode.INVITE_EXHAUSTED);
        }
    }
}
