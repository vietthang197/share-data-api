package com.thanglv.sharedataapi.controller;

import com.thanglv.sharedataapi.dto.request.LoginRequest;
import com.thanglv.sharedataapi.dto.request.RefreshTokenRequest;
import com.thanglv.sharedataapi.dto.request.RegisterAccountRequest;
import com.thanglv.sharedataapi.dto.response.BaseResponse;
import com.thanglv.sharedataapi.dto.response.LoginResponse;
import com.thanglv.sharedataapi.dto.response.RefreshTokenResponse;
import com.thanglv.sharedataapi.entity.Note;
import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.entity.UserRole;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
import com.thanglv.sharedataapi.repository.UserRoleRepository;
import com.thanglv.sharedataapi.services.UserAccountService;
import com.thanglv.sharedataapi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserAccountService userAccountService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return userAccountService.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> doRegister(@RequestBody RegisterAccountRequest request) {
        return userAccountService.registerAccount(request);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return userAccountService.refreshToken(request);
    }
}
