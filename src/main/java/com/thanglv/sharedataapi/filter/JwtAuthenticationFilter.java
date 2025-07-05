package com.thanglv.sharedataapi.filter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thanglv.sharedataapi.util.Constant;
import com.thanglv.sharedataapi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Gson gson;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(Constant.AUTHORIZATION_HEADER);
        if (authorization != null) {
            try {
                var claimsJws = jwtUtil.parseToken(authorization);
                // invalid token type
                if (!Constant.TOKEN_TYPE_ACCESS.equals(claimsJws.getHeader().getType())) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                } else {
                    String username = claimsJws.getPayload().getSubject();
                    ThreadContext.put(Constant.USER, username);
                    String roleString = claimsJws.getPayload().get(Constant.ROLE_CLAIM, String.class);
                    var authorities = gson.fromJson(roleString, new TypeToken<List<String>>() {}).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    var userToken = UsernamePasswordAuthenticationToken.authenticated(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(userToken);
                    filterChain.doFilter(request, response);
                }
            } catch (JwtException e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            } catch (Exception e) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else
            filterChain.doFilter(request, response);
    }
}
