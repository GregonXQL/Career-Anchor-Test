package com.careeranchor.server.enums;

public enum AnchorType {
    TECHNICAL(0, "技术/职能型"),
    MANAGERIAL(1, "管理型"),
    AUTONOMY(2, "自主/独立型"),
    SECURITY(3, "安全/稳定型"),
    CREATIVITY(4, "创造/创业型"),
    SERVICE(5, "服务型"),
    CHALLENGE(6, "挑战型"),
    LIFESTYLE(7, "生活型");

    private final int priority;
    private final String nameCn;

    AnchorType(int priority, String nameCn) {
        this.priority = priority;
        this.nameCn = nameCn;
    }

    public int priority() { return priority; }
    public String nameCn() { return nameCn; }

    public static AnchorType forQuestion(int questionId) {
        if (questionId < 1 || questionId > 40) {
            throw new IllegalArgumentException("questionId must be between 1 and 40");
        }
        return values()[(questionId - 1) % values().length];
    }
}
