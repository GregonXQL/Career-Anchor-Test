package com.careeranchor.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.careeranchor.server.enums.AnchorType;

import java.time.LocalDateTime;

@TableName("test_results")
public class TestResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long inviteCodeId;
    private Integer scaleMax;
    private String answers;
    private Integer scoreTechnical;
    private Integer scoreManagerial;
    private Integer scoreAutonomy;
    private Integer scoreSecurity;
    private Integer scoreCreativity;
    private Integer scoreService;
    private Integer scoreChallenge;
    private Integer scoreLifestyle;
    private AnchorType top1;
    private AnchorType top2;
    private AnchorType top3;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getInviteCodeId() { return inviteCodeId; }
    public void setInviteCodeId(Long inviteCodeId) { this.inviteCodeId = inviteCodeId; }
    public Integer getScaleMax() { return scaleMax; }
    public void setScaleMax(Integer scaleMax) { this.scaleMax = scaleMax; }
    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }
    public Integer getScoreTechnical() { return scoreTechnical; }
    public void setScoreTechnical(Integer scoreTechnical) { this.scoreTechnical = scoreTechnical; }
    public Integer getScoreManagerial() { return scoreManagerial; }
    public void setScoreManagerial(Integer scoreManagerial) { this.scoreManagerial = scoreManagerial; }
    public Integer getScoreAutonomy() { return scoreAutonomy; }
    public void setScoreAutonomy(Integer scoreAutonomy) { this.scoreAutonomy = scoreAutonomy; }
    public Integer getScoreSecurity() { return scoreSecurity; }
    public void setScoreSecurity(Integer scoreSecurity) { this.scoreSecurity = scoreSecurity; }
    public Integer getScoreCreativity() { return scoreCreativity; }
    public void setScoreCreativity(Integer scoreCreativity) { this.scoreCreativity = scoreCreativity; }
    public Integer getScoreService() { return scoreService; }
    public void setScoreService(Integer scoreService) { this.scoreService = scoreService; }
    public Integer getScoreChallenge() { return scoreChallenge; }
    public void setScoreChallenge(Integer scoreChallenge) { this.scoreChallenge = scoreChallenge; }
    public Integer getScoreLifestyle() { return scoreLifestyle; }
    public void setScoreLifestyle(Integer scoreLifestyle) { this.scoreLifestyle = scoreLifestyle; }
    public AnchorType getTop1() { return top1; }
    public void setTop1(AnchorType top1) { this.top1 = top1; }
    public AnchorType getTop2() { return top2; }
    public void setTop2(AnchorType top2) { this.top2 = top2; }
    public AnchorType getTop3() { return top3; }
    public void setTop3(AnchorType top3) { this.top3 = top3; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
