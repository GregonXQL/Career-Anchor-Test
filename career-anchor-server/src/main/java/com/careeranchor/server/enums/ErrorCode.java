package com.careeranchor.server.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(40001, HttpStatus.BAD_REQUEST, "参数校验失败"),
    UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "Token 缺失、无效或已过期"),
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "需要管理员权限"),
    INVALID_ANSWER(42001, HttpStatus.BAD_REQUEST, "答卷不完整或不合法"),
    WECHAT_LOGIN_FAILED(50001, HttpStatus.BAD_GATEWAY, "微信登录服务异常"),
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
