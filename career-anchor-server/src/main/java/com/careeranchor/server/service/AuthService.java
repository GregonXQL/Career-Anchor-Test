package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.dto.WxLoginResponse;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.mapper.UserMapper;
import com.careeranchor.server.util.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private final WxAuthClient wxAuthClient;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final String expectedAppId;

    public AuthService(WxAuthClient wxAuthClient, UserMapper userMapper, JwtUtil jwtUtil,
                       AppProperties properties) {
        this.wxAuthClient = wxAuthClient;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.expectedAppId = properties.wechat().appId();
    }

    @Transactional
    public WxLoginResponse wxLogin(String code, String cloudSource, String cloudAppId, String cloudOpenId) {
        String openid = resolveOpenId(code, cloudSource, cloudAppId, cloudOpenId);
        User user = findByOpenid(openid);
        if (user == null) {
            user = register(openid);
        }
        userMapper.touchLastLogin(user.getId());
        return new WxLoginResponse(jwtUtil.issue(user.getId(), Role.USER),
                new WxLoginResponse.UserView(user.getId(), user.getNickname(), null));
    }

    private String resolveOpenId(String code, String cloudSource, String cloudAppId, String cloudOpenId) {
        boolean hasCloudIdentity = StringUtils.hasText(cloudSource)
                || StringUtils.hasText(cloudAppId)
                || StringUtils.hasText(cloudOpenId);
        if (hasCloudIdentity) {
            if (!StringUtils.hasText(cloudSource)
                    || !StringUtils.hasText(cloudAppId)
                    || !StringUtils.hasText(cloudOpenId)) {
                throw new BizException(ErrorCode.WECHAT_LOGIN_FAILED, "微信云托管身份信息不完整");
            }
            if (!cloudAppId.equals(expectedAppId)) {
                throw new BizException(ErrorCode.WECHAT_LOGIN_FAILED, "微信云托管 AppID 不匹配");
            }
            return cloudOpenId;
        }
        if (!StringUtils.hasText(code)) {
            throw new BizException(ErrorCode.WECHAT_LOGIN_FAILED, "缺少微信登录凭证");
        }
        return wxAuthClient.exchangeCodeForOpenid(code);
    }

    private User register(String openid) {
        User user = new User();
        user.setOpenid(openid);
        try {
            userMapper.insert(user);
            return user;
        } catch (DuplicateKeyException exception) {
            // Two first-login requests may race; the openid unique key decides the winner.
            return findByOpenid(openid);
        }
    }

    private User findByOpenid(String openid) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getOpenid, openid));
    }
}
