package com.careeranchor.server.dto;

import com.careeranchor.server.enums.InviteChannel;

public record InviteVerifyResponse(boolean valid, InviteChannel channel) {
}
