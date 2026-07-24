package com.careeranchor.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.careeranchor.server.enums.InviteChannel;
import com.careeranchor.server.enums.InviteStatus;

import java.time.LocalDateTime;

@TableName("invite_codes")
public class InviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime expiresAt;
    private InviteStatus status;
    private InviteChannel channel;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime retiredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public InviteStatus getStatus() { return status; }
    public void setStatus(InviteStatus status) { this.status = status; }
    public InviteChannel getChannel() { return channel; }
    public void setChannel(InviteChannel channel) { this.channel = channel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getRetiredAt() { return retiredAt; }
    public void setRetiredAt(LocalDateTime retiredAt) { this.retiredAt = retiredAt; }
}
