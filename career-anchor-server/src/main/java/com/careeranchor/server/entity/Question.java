package com.careeranchor.server.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("questions")
public class Question {
    @TableId
    private Integer id;
    private String content;
    private String anchorCode;
    private Boolean enabled;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAnchorCode() { return anchorCode; }
    public void setAnchorCode(String anchorCode) { this.anchorCode = anchorCode; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
