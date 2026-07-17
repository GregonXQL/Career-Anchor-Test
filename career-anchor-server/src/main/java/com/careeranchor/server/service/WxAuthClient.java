package com.careeranchor.server.service;

import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WxAuthClient {
    private static final String CODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final RestClient restClient;
    private final AppProperties.Wechat properties;

    public WxAuthClient(RestClient restClient, AppProperties properties) {
        this.restClient = restClient;
        this.properties = properties.wechat();
    }

    public String exchangeCodeForOpenid(String code) {
        if (properties.mockEnabled()) {
            return "dev_" + Integer.toUnsignedString(code.hashCode(), 36);
        }
        if (!StringUtils.hasText(properties.appId()) || !StringUtils.hasText(properties.appSecret())) {
            throw new BizException(ErrorCode.WECHAT_LOGIN_FAILED, "微信 appid/secret 未配置");
        }
        try {
            CodeSessionResponse response = restClient.get()
                    .uri(CODE_TO_SESSION_URL + "?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code",
                            properties.appId(), properties.appSecret(), code)
                    .retrieve()
                    .body(CodeSessionResponse.class);
            if (response == null || response.errcode() != null || !StringUtils.hasText(response.openid())) {
                String detail = response == null ? "empty response" : response.errmsg();
                throw new BizException(ErrorCode.WECHAT_LOGIN_FAILED, "微信 code2Session 失败: " + detail);
            }
            return response.openid();
        } catch (BizException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BizException(ErrorCode.WECHAT_LOGIN_FAILED, "无法连接微信登录服务");
        }
    }

    private record CodeSessionResponse(
            String openid,
            @JsonProperty("session_key") String sessionKey,
            String unionid,
            Integer errcode,
            String errmsg) {
    }
}
