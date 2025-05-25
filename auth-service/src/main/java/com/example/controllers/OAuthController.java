package com.example.controllers;

import com.example.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/")
public class OAuthController {

    @Autowired
    private AuthService authService;

    // GET /oauth/login - получение кода авторизации
    @GetMapping("/oauth/login")
    public ResponseEntity<?> login(
            @RequestParam String client_id,
            @RequestParam String client_secret,
            @RequestParam String response_type,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String state) {

        if (!"code".equals(response_type)) {
            return ResponseEntity.badRequest().body("Unsupported response_type");
        }

        String code = authService.generateAuthorizationCode(client_id, client_secret, redirect_uri, state);
        if (code == null) {
            return ResponseEntity.status(401).body("Invalid client_id or client_secret");
        }

        // Возвращаем страницу подтверждения или сразу редирект с кодом
        // Для упрощения сразу редиректим на redirect_uri с кодом и state
        String redirectUrl = redirect_uri + "?code=" + code;
        if (state != null) {
            redirectUrl += "&state=" + state;
        }
        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }

    // POST /oauth/token - обмен кода на токен
    @PostMapping("/oauth/token")
    public ResponseEntity<?> token(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body("Missing code");
        }
        String token = authService.exchangeCodeForToken(code);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Collections.singletonMap("access_token", token));
    }

    // GET /api/v2/users/me - получение информации о пользователе по токену
    @GetMapping("/api/v2/users/me")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        String token = authService.extractToken(request);
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> userInfo = authService.getUserInfo(token);
        return ResponseEntity.ok(userInfo);
    }

    // POST /api/v2/users/hasAuthority - проверка прав пользователя
    @PostMapping("/api/v2/users/hasAuthority")
    public ResponseEntity<?> hasAuthority(HttpServletRequest request, @RequestBody List<String> roles) {
        String token = authService.extractToken(request);
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        boolean hasRoles = authService.hasRoles(token, roles);
        if (!hasRoles) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok("Когда права есть");
    }
}
