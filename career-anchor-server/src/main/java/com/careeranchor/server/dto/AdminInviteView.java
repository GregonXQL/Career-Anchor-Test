package com.careeranchor.server.dto;

import com.careeranchor.server.enums.InviteChannel;

public record AdminInviteView(long id, String code, InviteChannel channel,
                              int usedCount, int maxUses, String expiresAt,
                              String status, String remark, String createdAt) {}
