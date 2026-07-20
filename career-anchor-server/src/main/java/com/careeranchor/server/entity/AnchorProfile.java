package com.careeranchor.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.careeranchor.server.enums.AnchorType;

@TableName("anchor_profiles")
public class AnchorProfile {
    @TableId(value = "anchor_code", type = IdType.INPUT)
    private AnchorType anchorCode;
    private String nameCn;
    private String nameEn;
    private String tagline;
    private String traits;
    private String strengths;
    private String risks;
    private String advices;
    private String careers;

    public AnchorType getAnchorCode() { return anchorCode; }
    public void setAnchorCode(AnchorType anchorCode) { this.anchorCode = anchorCode; }
    public String getNameCn() { return nameCn; }
    public void setNameCn(String nameCn) { this.nameCn = nameCn; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getTraits() { return traits; }
    public void setTraits(String traits) { this.traits = traits; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getRisks() { return risks; }
    public void setRisks(String risks) { this.risks = risks; }
    public String getAdvices() { return advices; }
    public void setAdvices(String advices) { this.advices = advices; }
    public String getCareers() { return careers; }
    public void setCareers(String careers) { this.careers = careers; }
}
