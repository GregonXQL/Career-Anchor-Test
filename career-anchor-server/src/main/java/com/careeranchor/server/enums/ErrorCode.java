package com.careeranchor.server.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(40001, HttpStatus.BAD_REQUEST, "参数校验失败"),
    UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "Token 缺失、无效或已过期"),
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "需要管理员权限"),
    ADMIN_PASSWORD_INVALID(40302, HttpStatus.FORBIDDEN, "管理员密码错误"),
    ADMIN_LOGIN_LOCKED(40303, HttpStatus.FORBIDDEN, "密码错误次数过多，请稍后再试"),
    INVITE_NOT_FOUND(41001, HttpStatus.BAD_REQUEST, "邀请码不存在"),
    INVITE_EXPIRED(41002, HttpStatus.BAD_REQUEST, "邀请码已过期"),
    INVITE_EXHAUSTED(41003, HttpStatus.BAD_REQUEST, "邀请码已被使用或次数已用尽"),
    INVITE_DISABLED(41004, HttpStatus.BAD_REQUEST, "邀请码已停用"),
    INVALID_ANSWER(42001, HttpStatus.BAD_REQUEST, "答卷不完整或不合法"),
    SUBMIT_TOO_FREQUENTLY(42002, HttpStatus.TOO_MANY_REQUESTS, "提交过于频繁，请稍后再试"),
    RESULT_NOT_FOUND(43001, HttpStatus.NOT_FOUND, "结果不存在或无权查看"),
    WECHAT_LOGIN_FAILED(50001, HttpStatus.BAD_GATEWAY, "微信登录服务异常"),
    WECHAT_ACODE_FAILED(50002, HttpStatus.BAD_GATEWAY, "微信小程序码生成服务异常"),
    INTERNAL_ERROR(59999, HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int code() { return code; }
    public HttpStatus status() { return status; }
    public String message() { return message; }
}
