package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.dto.AdminLoginResponse;
import com.careeranchor.server.entity.AdminAccount;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.AdminAccountMapper;
import com.careeranchor.server.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    private final AdminAccountMapper adminAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminLoginGuard loginGuard;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminAccountMapper adminAccountMapper, PasswordEncoder passwordEncoder,
                            AdminLoginGuard loginGuard, JwtUtil jwtUtil) {
        this.adminAccountMapper = adminAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginGuard = loginGuard;
        this.jwtUtil = jwtUtil;
    }

    public AdminLoginResponse login(long userId, String password) {
        if (loginGuard.isLocked(userId)) throw new BizException(ErrorCode.ADMIN_LOGIN_LOCKED);
        AdminAccount account = adminAccountMapper.selectOne(Wrappers.<AdminAccount>lambdaQuery()
                .orderByAsc(AdminAccount::getId).last("LIMIT 1"));
        if (account == null || !passwordEncoder.matches(password, account.getPasswordHash())) {
            if (loginGuard.recordFailure(userId)) throw new BizException(ErrorCode.ADMIN_LOGIN_LOCKED);
            throw new BizException(ErrorCode.ADMIN_PASSWORD_INVALID);
        }
        loginGuard.clear(userId);
        return new AdminLoginResponse(jwtUtil.issue(account.getId(), Role.ADMIN));
    }
}
