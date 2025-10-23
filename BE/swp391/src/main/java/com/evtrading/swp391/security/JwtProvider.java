package com.evtrading.swp391.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    // accept either app.jwt.secret or app.jwtSecret (fallback)
    @Value("${app.jwt.secret:${app.jwtSecret:}}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:${app.jwtExpirationMs:86400000}}")
    private long jwtExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    private void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret not configured (app.jwt.secret or app.jwtSecret)");
        }
        byte[] keyBytes = decodeSecret(jwtSecret.trim());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Integer userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", new String(email.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
                .claim("username", new String(email.split("@")[0].getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)) 
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    private byte[] decodeSecret(String s) {
        // hex detection
        if (s.matches("^[0-9A-Fa-f]+$") && s.length() % 2 == 0) {
            return hexStringToByteArray(s);
        }
        // try base64
        try {
            return Decoders.BASE64.decode(s);
        } catch (Exception ignored) {
            // fallback to UTF-8 bytes
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
