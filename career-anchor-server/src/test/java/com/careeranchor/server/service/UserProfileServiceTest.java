package com.careeranchor.server.service;

import com.careeranchor.server.dto.UserProfileResponse;
import com.careeranchor.server.dto.UserProfileUpdateRequest;
import com.careeranchor.server.entity.User;
import com.careeranchor.server.mapper.UserMapper;
import com.careeranchor.server.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {
    @Test
    void updatesTheNicknameUsedByCurrentAndHistoricalAdminResults() {
        UserMapper mapper = mock(UserMapper.class);
        User user = new User();
        user.setId(7L);
        user.setOpenid("openid");
        when(mapper.selectById(7L)).thenReturn(user);
        UserProfileService service = new UserProfileService(mapper);

        String avatar = "data:image/jpeg;base64,aGVsbG8=";
        UserProfileResponse response = service.update(7L, new UserProfileUpdateRequest("  小林  ", avatar));

        assertThat(response.nickname()).isEqualTo("小林");
        assertThat(response.avatarUrl()).isEqualTo(avatar);
        assertThat(user.getNickname()).isEqualTo("小林");
        assertThat(user.getAvatarUrl()).isEqualTo(avatar);
        verify(mapper).updateById(user);
    }

    @Test
    void rejectsTheGenericWechatPlaceholder() {
        UserMapper mapper = mock(UserMapper.class);
        User user = new User();
        user.setId(7L);
        when(mapper.selectById(7L)).thenReturn(user);
        UserProfileService service = new UserProfileService(mapper);

        assertThatThrownBy(() -> service.update(7L,
                new UserProfileUpdateRequest("微信用户", "data:image/jpeg;base64,aGVsbG8=")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("区分报告");
    }
}
