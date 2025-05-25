package com.example.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class AuthService {

    @Value("${custom.jwt.secret}")
    private String secret;

    // Хранилище для кодов авторизации (authorization codes)
    private Map<String, String> authorizationCodes; // code -> client_id

    // Хранилище для клиентов (client_id -> client_secret)
    private Map<String, String> clients;

    // Хранилище для пользователей (client_id -> user info)
    private Map<String, Map<String, Object>> users;

    @PostConstruct
    public void init() {
        authorizationCodes = new HashMap<>();
        clients = new HashMap<>();
        users = new HashMap<>();

        // Пример клиента
        clients.put("client1", "secret1");

        // Пример пользователя для client1 с упрощенной информацией
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", "Name");
        userInfo.put("role", "ROLE_USER");
        userInfo.put("admin", true);
        users.put("client1", userInfo);
    }

    public String generateAuthorizationCode(String clientId, String clientSecret, String redirectUri, String state) {
        if (!clients.containsKey(clientId)) {
            return null;
        }
        if (!clients.get(clientId).equals(clientSecret)) {
            return null;
        }
        // Генерируем код авторизации
        String code = UUID.randomUUID().toString();
        authorizationCodes.put(code, clientId);
        return code;
    }

    public String exchangeCodeForToken(String code) {
        if (!authorizationCodes.containsKey(code)) {
            return null;
        }
        String clientId = authorizationCodes.get(code);
        Map<String, Object> userInfo = users.get(clientId);
        if (userInfo == null) {
            return null;
        }
        // Создаем JWT токен с упрощенной информацией
        String token = Jwts.builder()
                .setSubject((String) userInfo.get("name"))
                .claim("role", userInfo.get("role"))
                .claim("admin", userInfo.get("admin"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 день
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();

        // Удаляем использованный код
        authorizationCodes.remove(code);
        return token;
    }

    public String extractToken(HttpServletRequest request) {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Попытка получить токен из cookie
            if (request.getCookies() != null) {
                for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        return token;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getUserInfo(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", claims.getSubject());
            userInfo.put("role", claims.get("role"));
            userInfo.put("admin", claims.get("admin"));
            return userInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasRoles(String token, List<String> rolesToCheck) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
            String role = (String) claims.get("role");
            if (role == null) {
                return false;
            }
            for (String roleToCheck : rolesToCheck) {
                if (!role.equals(roleToCheck)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
