package com.example.config;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomOAuth2SuccessHandlerTest {

    private static final String SECRET = "01234567890123456789012345678901"; // 32 chars for HS256

    private CustomOAuth2SuccessHandler successHandler;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter responseWriter;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        successHandler = new CustomOAuth2SuccessHandler(SECRET);

        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("user@example.com");

        GrantedAuthority authority = () -> "ROLE_USER";
        when(authentication.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(authority));
    }

    @Test
    public void testOnAuthenticationSuccess_WritesJwtToken() throws Exception {
        successHandler.onAuthenticationSuccess(request, response, authentication);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("\"token\":"));

        // Проверка, что токен валидный JWT
        String token = responseContent.replace("{\"token\": \"", "").replace("\"}", "");
        assertNotNull(token);

        // Парсим токен, чтобы проверить subject и claims
        io.jsonwebtoken.Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("user@example.com", claims.getSubject());
        assertTrue(((List<?>) claims.get("authorities")).contains("ROLE_USER"));
    }
}
