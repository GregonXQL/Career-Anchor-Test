package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.dto.AdminInviteView;
import com.careeranchor.server.dto.AdminResultSummary;
import com.careeranchor.server.dto.AdminStatsResponse;
import com.careeranchor.server.dto.InviteCreateRequest;
import com.careeranchor.server.dto.PageResponse;
import com.careeranchor.server.dto.QrImageResponse;
import com.careeranchor.server.dto.QrInviteCreateRequest;
import com.careeranchor.server.dto.QrInviteResponse;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.entity.InviteCode;
import com.careeranchor.server.entity.TestResult;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.InviteChannel;
import com.careeranchor.server.enums.InviteStatus;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.InviteCodeMapper;
import com.careeranchor.server.mapper.TestResultMapper;
import com.careeranchor.server.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> INVITE_FILTERS = Set.of("ALL", "ACTIVE", "USED", "EXPIRED", "DISABLED");
    private static final int INSERT_RETRIES = 20;

    private final TestResultMapper testResultMapper;
    private final UserMapper userMapper;
    private final InviteCodeMapper inviteCodeMapper;
    private final InviteCodeGenerator codeGenerator;
    private final InviteService inviteService;
    private final ResultService resultService;
    private final WxAcodeService wxAcodeService;
    private final Clock clock;

    public AdminService(TestResultMapper testResultMapper, UserMapper userMapper,
                        InviteCodeMapper inviteCodeMapper, InviteCodeGenerator codeGenerator,
                        InviteService inviteService, ResultService resultService,
                        WxAcodeService wxAcodeService, Clock clock) {
        this.testResultMapper = testResultMapper;
        this.userMapper = userMapper;
        this.inviteCodeMapper = inviteCodeMapper;
        this.codeGenerator = codeGenerator;
        this.inviteService = inviteService;
        this.resultService = resultService;
        this.wxAcodeService = wxAcodeService;
        this.clock = clock;
    }

    public AdminStatsResponse stats() {
        LocalDate today = LocalDate.now(clock);
        long activeInvites = inviteCodeMapper.selectList(Wrappers.<InviteCode>lambdaQuery()
                        .eq(InviteCode::getStatus, InviteStatus.ACTIVE)
                        .isNull(InviteCode::getRetiredAt))
                .stream().filter(invite -> "ACTIVE".equals(effectiveStatus(invite))).count();
        return new AdminStatsResponse(
                testResultMapper.selectCount(Wrappers.<TestResult>emptyWrapper()),
                testResultMapper.countCreatedBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay()),
                userMapper.selectCount(Wrappers.<User>emptyWrapper()),
                activeInvites);
    }

    public PageResponse<AdminResultSummary> results(int page, int size, String keyword,
                                                     LocalDate from, LocalDate to) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.strip();
        LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toExclusive = to == null ? null : to.plusDays(1).atStartOfDay();
        long total = testResultMapper.countAdminResults(normalized, fromTime, toExclusive);
        long offset = (long) (page - 1) * size;
        return new PageResponse<>(total, page, size,
                testResultMapper.selectAdminResults(normalized, fromTime, toExclusive, offset, size));
    }

    public ReportResponse result(long id) {
        return resultService.get(id, new AuthContext.Principal(0, Role.ADMIN));
    }

    @Transactional
    public List<AdminInviteView> createManual(long adminId, InviteCreateRequest request) {
        List<AdminInviteView> created = new ArrayList<>();
        for (int index = 0; index < request.count(); index += 1) {
            created.add(toView(insert(adminId, InviteChannel.MANUAL, 1,
                    request.expiresAt(), request.remark())));
        }
        return List.copyOf(created);
    }

    @Transactional
    public QrInviteResponse createQr(long adminId, QrInviteCreateRequest request) {
        int maxUses = request.maxUses() == null ? 1 : request.maxUses();
        InviteCode invite = insert(adminId, InviteChannel.QR, maxUses,
                request.expiresAt(), request.remark());
        return new QrInviteResponse(toView(invite), wxAcodeService.generate(invite.getCode()));
    }

    public PageResponse<AdminInviteView> invites(int page, int size, String status) {
        String filter = status == null || status.isBlank() ? "ALL" : status.toUpperCase(Locale.ROOT);
        if (!INVITE_FILTERS.contains(filter)) throw new BizException(ErrorCode.VALIDATION_FAILED);
        List<AdminInviteView> matches = inviteCodeMapper.selectList(Wrappers.<InviteCode>lambdaQuery()
                        .isNull(InviteCode::getRetiredAt)
                        .orderByDesc(InviteCode::getCreatedAt).orderByDesc(InviteCode::getId)).stream()
                .map(this::toView)
                .filter(invite -> "ALL".equals(filter) || invite.status().equals(filter))
                .toList();
        int from = Math.min(matches.size(), (page - 1) * size);
        int to = Math.min(matches.size(), from + size);
        return new PageResponse<>(matches.size(), page, size, List.copyOf(matches.subList(from, to)));
    }

    public AdminInviteView disable(long id) {
        InviteCode invite = findInvite(id);
        invite.setStatus(InviteStatus.DISABLED);
        inviteCodeMapper.updateById(invite);
        return toView(findInvite(id));
    }

    public QrImageResponse qrcode(long id) {
        InviteCode invite = findInvite(id);
        inviteService.verify(invite.getCode());
        return new QrImageResponse(wxAcodeService.generate(invite.getCode()));
    }

    private InviteCode insert(long adminId, InviteChannel channel, int maxUses,
                              LocalDateTime expiresAt, String remark) {
        for (int attempt = 0; attempt < INSERT_RETRIES; attempt += 1) {
            InviteCode invite = new InviteCode();
            invite.setCode(codeGenerator.next());
            invite.setMaxUses(maxUses);
            invite.setUsedCount(0);
            invite.setExpiresAt(expiresAt);
            invite.setStatus(InviteStatus.ACTIVE);
            invite.setChannel(channel);
            invite.setRemark(remark == null || remark.isBlank() ? null : remark.strip());
            invite.setCreatedBy(adminId);
            try {
                inviteCodeMapper.insert(invite);
                return findInvite(invite.getId());
            } catch (DuplicateKeyException ignored) {
                // Secure random collisions are unlikely; retry with a new code.
            }
        }
        throw new IllegalStateException("Unable to generate a unique invite code");
    }

    private InviteCode findInvite(long id) {
        InviteCode invite = inviteCodeMapper.selectOne(Wrappers.<InviteCode>lambdaQuery()
                .eq(InviteCode::getId, id)
                .isNull(InviteCode::getRetiredAt));
        if (invite == null) throw new BizException(ErrorCode.INVITE_NOT_FOUND);
        return invite;
    }

    private AdminInviteView toView(InviteCode invite) {
        return new AdminInviteView(invite.getId(), invite.getCode(), invite.getChannel(),
                invite.getUsedCount(), invite.getMaxUses(), format(invite.getExpiresAt()),
                effectiveStatus(invite), invite.getRemark(), format(invite.getCreatedAt()));
    }

    private String effectiveStatus(InviteCode invite) {
        if (invite.getStatus() == InviteStatus.DISABLED) return "DISABLED";
        LocalDateTime now = LocalDateTime.now(clock);
        if (invite.getExpiresAt() != null && !invite.getExpiresAt().isAfter(now)) return "EXPIRED";
        if (invite.getUsedCount() >= invite.getMaxUses()) return "USED";
        return "ACTIVE";
    }

    private String format(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME);
    }
}
