package com.careeranchor.server.service;

import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

@Service
public class WxAcodeService {
    private static final long REFRESH_ADVANCE_SECONDS = 300;
    private static final Logger log = LoggerFactory.getLogger(WxAcodeService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AppProperties.Wechat properties;
    private final Clock clock;
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();
    private final Object tokenLock = new Object();

    public WxAcodeService(RestClient restClient, ObjectMapper objectMapper,
                          AppProperties appProperties, Clock clock) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.properties = appProperties.wechat();
        this.clock = clock;
    }

    public String generate(String inviteCode) {
        byte[] png = properties.mockEnabled() ? mockPng(inviteCode) : requestPng(inviteCode);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }

    private byte[] requestPng(String inviteCode) {
        String endpoint;
        if (properties.cloudCallEnabled()) {
            endpoint = properties.apiBaseUrl() + "/wxa/getwxacodeunlimit";
        } else {
            requireCredentials();
            endpoint = properties.apiBaseUrl() + "/wxa/getwxacodeunlimit?access_token=" + accessToken();
        }
        byte[] response;
        try {
            response = restClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "scene", "i=" + inviteCode,
                            "page", "pages/quiz/index",
                            "check_path", properties.acodeCheckPath(),
                            "env_version", properties.acodeEnvVersion()))
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientResponseException exception) {
            String detail = responseDetail(exception.getResponseBodyAsByteArray());
            log.warn("WeChat mini program code request failed with HTTP {}: {}", exception.getStatusCode(), detail);
            throw acodeFailure(detail);
        } catch (RuntimeException exception) {
            log.warn("WeChat mini program code request failed", exception);
            String detail = properties.cloudCallEnabled() ? "无法连接微信云调用代理" : "无法连接微信服务";
            throw acodeFailure(detail);
        }
        if (response == null || response.length < 8 || isJson(response)) {
            String detail = responseDetail(response);
            log.warn("WeChat mini program code returned an invalid response: {}", detail);
            throw acodeFailure(detail);
        }
        return response;
    }

    private String accessToken() {
        Instant now = Instant.now(clock);
        CachedToken current = cachedToken.get();
        if (current != null && now.isBefore(current.refreshAt())) return current.value();
        synchronized (tokenLock) {
            current = cachedToken.get();
            now = Instant.now(clock);
            if (current != null && now.isBefore(current.refreshAt())) return current.value();
            try {
                JsonNode response = restClient.get()
                        .uri(properties.apiBaseUrl() + "/cgi-bin/token?grant_type=client_credential&appid="
                                + properties.appId() + "&secret=" + properties.appSecret())
                        .retrieve()
                        .body(JsonNode.class);
                String token = response == null ? "" : response.path("access_token").asText();
                long expiresIn = response == null ? 0 : response.path("expires_in").asLong();
                if (token.isBlank() || expiresIn <= REFRESH_ADVANCE_SECONDS) {
                    throw new BizException(ErrorCode.WECHAT_ACODE_FAILED);
                }
                CachedToken fresh = new CachedToken(token,
                        now.plusSeconds(expiresIn - REFRESH_ADVANCE_SECONDS));
                cachedToken.set(fresh);
                return fresh.value();
            } catch (BizException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new BizException(ErrorCode.WECHAT_ACODE_FAILED);
            }
        }
    }

    private void requireCredentials() {
        if (properties.appId() == null || properties.appId().isBlank()
                || properties.appSecret() == null || properties.appSecret().isBlank()) {
            throw new BizException(ErrorCode.WECHAT_ACODE_FAILED);
        }
    }

    private boolean isJson(byte[] response) {
        int index = 0;
        while (index < response.length && Character.isWhitespace(response[index])) index += 1;
        if (index >= response.length || response[index] != '{') return false;
        try {
            JsonNode error = objectMapper.readTree(response);
            return error.has("errcode");
        } catch (IOException ignored) {
            return true;
        }
    }

    private String responseDetail(byte[] response) {
        if (response == null) return "empty response";
        try {
            JsonNode json = objectMapper.readTree(response);
            return "errcode=" + json.path("errcode").asText() + ", errmsg=" + json.path("errmsg").asText();
        } catch (IOException ignored) {
            return "unexpected response, bytes=" + response.length;
        }
    }

    private BizException acodeFailure(String detail) {
        return new BizException(ErrorCode.WECHAT_ACODE_FAILED, "微信小程序码生成失败：" + detail);
    }

    private byte[] mockPng(String code) {
        final int size = 256;
        final int modules = 29;
        final int cell = 7;
        final int margin = (size - modules * cell) / 2;
        byte[] raw = new byte[size * (size * 3 + 1)];
        int offset = 0;
        int seed = code.hashCode();
        for (int y = 0; y < size; y += 1) {
            raw[offset++] = 0;
            for (int x = 0; x < size; x += 1) {
                int moduleX = (x - margin) / cell;
                int moduleY = (y - margin) / cell;
                boolean inside = x >= margin && y >= margin && moduleX < modules && moduleY < modules;
                boolean dark = inside && (finder(moduleX, moduleY, 0, 0)
                        || finder(moduleX, moduleY, modules - 7, 0)
                        || finder(moduleX, moduleY, 0, modules - 7)
                        || Math.floorMod(seed ^ moduleX * 31 ^ moduleY * 131, 5) < 2);
                raw[offset++] = (byte) (dark ? 0x8A : 0xF5);
                raw[offset++] = (byte) (dark ? 0x6A : 0xF2);
                raw[offset++] = (byte) (dark ? 0x4F : 0xED);
            }
        }
        try {
            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
                deflater.write(raw);
            }
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(png);
            output.write(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            ByteArrayOutputStream header = new ByteArrayOutputStream();
            DataOutputStream headerData = new DataOutputStream(header);
            headerData.writeInt(size);
            headerData.writeInt(size);
            headerData.writeByte(8);
            headerData.writeByte(2);
            headerData.writeByte(0);
            headerData.writeByte(0);
            headerData.writeByte(0);
            writeChunk(output, "IHDR", header.toByteArray());
            writeChunk(output, "IDAT", compressed.toByteArray());
            writeChunk(output, "IEND", new byte[0]);
            return png.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate development mini program code", exception);
        }
    }

    private boolean finder(int x, int y, int left, int top) {
        int localX = x - left;
        int localY = y - top;
        if (localX < 0 || localX >= 7 || localY < 0 || localY >= 7) return false;
        return localX == 0 || localX == 6 || localY == 0 || localY == 6
                || (localX >= 2 && localX <= 4 && localY >= 2 && localY <= 4);
    }

    private void writeChunk(DataOutputStream output, String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        output.writeInt(data.length);
        output.write(typeBytes);
        output.write(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        output.writeInt((int) crc.getValue());
    }

    record CachedToken(String value, Instant refreshAt) {}
}
