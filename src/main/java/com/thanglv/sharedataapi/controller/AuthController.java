package com.thanglv.sharedataapi.controller;

import com.thanglv.sharedataapi.dto.request.LoginRequest;
import com.thanglv.sharedataapi.dto.request.RefreshTokenRequest;
import com.thanglv.sharedataapi.dto.request.RegisterAccountRequest;
import com.thanglv.sharedataapi.dto.response.LoginResponse;
import com.thanglv.sharedataapi.dto.response.RefreshTokenResponse;
import com.thanglv.sharedataapi.entity.Note;
import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
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
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwtUtil.generateAccessToken((User) authentication.getPrincipal()));
            response.setRefreshToken(jwtUtil.generateRefreshToken((User) authentication.getPrincipal()));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> doRegister(@RequestBody RegisterAccountRequest request) {
        Optional<UserAccount> accountOptional = userAccountRepository.findByEmail(request.getEmail().toLowerCase());
        if (accountOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account exists!");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(request.getEmail().toLowerCase());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setIsLock("N");

        userAccountRepository.save(userAccount);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            Jws<Claims> claimsJws = jwtUtil.parseToken(request.getRefreshToken());
            if (!"refresh_token".equals(claimsJws.getHeader().getType())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(claimsJws.getPayload().getSubject());
            if (userAccountOptional.isPresent()) {
                UserAccount userAccount = userAccountOptional.get();
                User user = new User(userAccount.getEmail(), userAccount.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
                RefreshTokenResponse response = new RefreshTokenResponse();
                response.setAccessToken(jwtUtil.generateAccessToken(user));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
