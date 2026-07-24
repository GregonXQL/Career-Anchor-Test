package com.careeranchor.server.service;

import com.careeranchor.server.dto.UserProfileResponse;
import com.careeranchor.server.dto.UserProfileUpdateRequest;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
public class UserProfileService {
    private static final int MAX_AVATAR_BYTES = 256 * 1024;

    private final UserMapper userMapper;

    public UserProfileService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserProfileResponse get(long userId) {
        return toResponse(find(userId));
    }

    @Transactional
    public UserProfileResponse update(long userId, UserProfileUpdateRequest request) {
        User user = find(userId);
        String nickname = request.nickname().strip();
        if ("微信用户".equals(nickname)) {
            throw new BizException(ErrorCode.USER_PROFILE_REQUIRED, "请输入可用于区分报告的昵称");
        }
        validateAvatar(request.avatarUrl());
        user.setNickname(nickname);
        user.setAvatarUrl(request.avatarUrl());
        userMapper.updateById(user);
        return toResponse(user);
    }

    private User find(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return user;
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getNickname(), user.getAvatarUrl());
    }

    private void validateAvatar(String avatarUrl) {
        int separator = avatarUrl.indexOf(',');
        String header = separator < 0 ? "" : avatarUrl.substring(0, separator);
        if (!header.matches("^data:image/(jpeg|png|webp);base64$")) {
            throw new BizException(ErrorCode.USER_PROFILE_REQUIRED, "头像格式不支持");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(avatarUrl.substring(separator + 1));
            if (bytes.length == 0 || bytes.length > MAX_AVATAR_BYTES) {
                throw new BizException(ErrorCode.USER_PROFILE_REQUIRED, "头像文件不能超过 256KB");
            }
        } catch (IllegalArgumentException exception) {
            throw new BizException(ErrorCode.USER_PROFILE_REQUIRED, "头像数据无效");
        }
    }
}
