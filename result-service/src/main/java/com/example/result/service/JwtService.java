package com.example.result.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Auth Service stores roles as [{"authority":"VOTER"}]
    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        Object roles = extractAllClaims(token).get("roles");
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles instanceof List<?> list) {
            for (Object role : list) {
                if (role instanceof Map<?, ?> map) {
                    authorities.add(new SimpleGrantedAuthority(String.valueOf(map.get("authority"))));
                } else {
                    authorities.add(new SimpleGrantedAuthority(String.valueOf(role)));
                }
            }
        }
        return authorities;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)   // checks signature and expiry
                .getBody();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}