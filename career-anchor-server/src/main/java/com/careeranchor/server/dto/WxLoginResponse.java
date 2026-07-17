package com.careeranchor.server.dto;

public record WxLoginResponse(String token, UserView user) {
    public record UserView(long id, String nickname, String avatarUrl) {}
}
