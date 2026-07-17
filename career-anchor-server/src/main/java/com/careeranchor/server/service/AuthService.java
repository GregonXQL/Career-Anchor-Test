package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.dto.WxLoginResponse;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.mapper.UserMapper;
import com.careeranchor.server.util.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final WxAuthClient wxAuthClient;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthService(WxAuthClient wxAuthClient, UserMapper userMapper, JwtUtil jwtUtil) {
        this.wxAuthClient = wxAuthClient;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public WxLoginResponse wxLogin(String code) {
        String openid = wxAuthClient.exchangeCodeForOpenid(code);
        User user = findByOpenid(openid);
        if (user == null) {
            user = register(openid);
        }
        userMapper.touchLastLogin(user.getId());
        return new WxLoginResponse(jwtUtil.issue(user.getId(), Role.USER),
                new WxLoginResponse.UserView(user.getId(), user.getNickname(), user.getAvatarUrl()));
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
