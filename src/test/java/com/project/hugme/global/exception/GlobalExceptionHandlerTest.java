package com.project.hugme.global.exception;

import com.project.hugme.domain.auth.exception.RefreshTokenReuseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void refreshTokenReuse_401응답과_쿠키삭제헤더를_반환한다()
            throws Exception {

        mockMvc.perform(post("/test/refresh-token-reuse"))
                .andExpect(status().isUnauthorized())

                // 에러 응답 검증
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code")
                        .value("REFRESH_TOKEN_REUSED"))

                // Refresh Token 쿠키 삭제 검증
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        allOf(
                                containsString("refreshToken="),
                                containsString("Max-Age=0"),
                                containsString("Path=/api/auth"),
                                containsString("HttpOnly"),
                                containsString("Secure")
                        )
                ));
    }

    @RestController
    static class TestController {

        @PostMapping("/test/refresh-token-reuse")
        void refreshTokenReuse() {
            throw new RefreshTokenReuseException();
        }
    }
}