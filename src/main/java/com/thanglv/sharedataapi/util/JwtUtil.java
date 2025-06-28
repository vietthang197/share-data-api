package com.thanglv.sharedataapi.util;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private static final String SECRET = "23213-23123rwrewr-2423432423-rwr245324t";

    private final Gson gson;

    public String generateToken(User account) {
        SimpleGrantedAuthority defaultAuthority = new SimpleGrantedAuthority("USER");
        Date now = new Date();
        return Jwts.builder()
                .subject(account.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 86400))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .claim("ROLE", gson.toJson(List.of(defaultAuthority)))
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(token);
    }
}
