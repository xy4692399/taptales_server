package com.taptales.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    private byte[] getKeyBytes() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(secret.getBytes("UTF-8"));
        } catch (Exception e) {
            return secret.getBytes();
        }
    }

    public String generateToken(Long userId, String type) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, getKeyBytes())
                .compact();
    }

    public Long parseUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public String parseType(String token) {
        return parseToken(token).get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getKeyBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}