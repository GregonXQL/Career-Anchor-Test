package com.careeranchor.server.controller;

import com.careeranchor.server.dto.WxLoginResponse;
import com.careeranchor.server.service.AdminAuthService;
import com.careeranchor.server.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {
    @Test
    void acceptsEmptyCloudRunBodyWhenIdentityHeadersArePresent() throws Exception {
        AuthService authService = mock(AuthService.class);
        WxLoginResponse response = new WxLoginResponse("token", new WxLoginResponse.UserView(7L, null, null));
        when(authService.wxLogin(null, "wx", "wx-test-app", "cloud-openid")).thenReturn(response);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, mock(AdminAuthService.class)))
                .build();

        mockMvc.perform(post("/auth/wx-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("X-WX-SOURCE", "wx")
                        .header("X-WX-APPID", "wx-test-app")
                        .header("X-WX-OPENID", "cloud-openid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("token"));

        verify(authService).wxLogin(null, "wx", "wx-test-app", "cloud-openid");
    }
}
