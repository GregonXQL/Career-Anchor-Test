package com.careeranchor.server.service;

import com.careeranchor.server.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.twice;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.http.HttpMethod.POST;

class WxAcodeServiceTest {
    @Test
    void developmentModeReturnsAValidPngDataUrlWithoutCallingWechat() throws Exception {
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", java.time.Duration.ofDays(7), java.time.Duration.ofHours(2)),
                new AppProperties.Wechat("", "", true, false, "https://api.weixin.qq.com", "develop", false),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));
        WxAcodeService service = new WxAcodeService(mock(RestClient.class), new ObjectMapper(), properties, Clock.systemUTC());

        String image = service.generate("M3TEST88");
        byte[] png = Base64.getDecoder().decode(image.substring("data:image/png;base64,".length()));

        assertThat(image).startsWith("data:image/png;base64,");
        assertThat(png).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
        assertThat(ByteBuffer.wrap(png, 16, 4).getInt()).isEqualTo(256);
    }

    @Test
    void productionModeCachesAccessTokenAndRequestsOneCodePerCall() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
        server.expect(once(), requestTo("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=app&secret=secret"))
                .andRespond(withSuccess("{\"access_token\":\"cached-token\",\"expires_in\":7200}", MediaType.APPLICATION_JSON));
        server.expect(twice(), requestTo("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=cached-token"))
                .andExpect(method(POST))
                .andRespond(withSuccess(png, MediaType.IMAGE_PNG));
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", java.time.Duration.ofDays(7), java.time.Duration.ofHours(2)),
                new AppProperties.Wechat("app", "secret", false, false, "https://api.weixin.qq.com", "trial", false),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));
        WxAcodeService service = new WxAcodeService(builder.build(), new ObjectMapper(), properties,
                Clock.fixed(Instant.parse("2026-07-20T00:00:00Z"), ZoneOffset.UTC));

        assertThat(service.generate("M3TEST88")).startsWith("data:image/png;base64,");
        assertThat(service.generate("M3TEST89")).startsWith("data:image/png;base64,");
        server.verify();
    }

    @Test
    void cloudRunModeUsesTheTokenFreeWechatProxy() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
        server.expect(once(), requestTo("http://api.weixin.qq.com/wxa/getwxacodeunlimit"))
                .andExpect(method(POST))
                .andRespond(withSuccess(png, MediaType.IMAGE_PNG));
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", java.time.Duration.ofDays(7), java.time.Duration.ofHours(2)),
                new AppProperties.Wechat("", "", false, true, "http://api.weixin.qq.com", "trial", true),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));
        WxAcodeService service = new WxAcodeService(builder.build(), new ObjectMapper(), properties, Clock.systemUTC());

        assertThat(service.generate("M3TEST88")).startsWith("data:image/png;base64,");
        server.verify();
    }

    @Test
    void cloudRunModeReturnsWechatErrorDetailsToTheAdmin() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("http://api.weixin.qq.com/wxa/getwxacodeunlimit"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"errcode\":41030,\"errmsg\":\"invalid page\"}"));
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", java.time.Duration.ofDays(7), java.time.Duration.ofHours(2)),
                new AppProperties.Wechat("", "", false, true, "http://api.weixin.qq.com", "trial", true),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));
        WxAcodeService service = new WxAcodeService(builder.build(), new ObjectMapper(), properties, Clock.systemUTC());

        assertThatThrownBy(() -> service.generate("M3TEST88"))
                .hasMessageContaining("errcode=41030")
                .hasMessageContaining("invalid page");
        server.verify();
    }
}
