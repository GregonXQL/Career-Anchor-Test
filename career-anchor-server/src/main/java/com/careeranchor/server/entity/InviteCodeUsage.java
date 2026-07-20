package com.careeranchor.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("invite_code_usages")
public class InviteCodeUsage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long inviteCodeId;
    private Long userId;
    private LocalDateTime usedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInviteCodeId() { return inviteCodeId; }
    public void setInviteCodeId(Long inviteCodeId) { this.inviteCodeId = inviteCodeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
