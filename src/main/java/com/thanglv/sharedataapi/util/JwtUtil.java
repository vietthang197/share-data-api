package com.thanglv.sharedataapi.util;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final Environment environment;

    private final Gson gson;

    public String generateAccessToken(User account) {
        Date now = new Date();
        return Jwts.builder()
                .header().type("access_token").and()
                .subject(account.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 86400))
                .signWith(Keys.hmacShaKeyFor(Objects.requireNonNull(environment.getProperty("access_token.secret")).getBytes(StandardCharsets.UTF_8)))
                .claim("role", gson.toJson(List.of("USER")))
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(Objects.requireNonNull(environment.getProperty("access_token.secret")).getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(token);
    }

    public String generateRefreshToken(User account) {
        Date now = new Date();
        return Jwts.builder()
                .header().type("refresh_token").and()
                .subject(account.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 86400))
                .signWith(Keys.hmacShaKeyFor(Objects.requireNonNull(environment.getProperty("access_token.secret")).getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
