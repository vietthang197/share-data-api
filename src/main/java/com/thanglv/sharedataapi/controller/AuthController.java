package com.thanglv.sharedataapi.controller;

import com.thanglv.sharedataapi.dto.request.LoginRequest;
import com.thanglv.sharedataapi.dto.request.RegisterAccountRequest;
import com.thanglv.sharedataapi.dto.response.LoginResponse;
import com.thanglv.sharedataapi.entity.Note;
import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
import com.thanglv.sharedataapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController("/api/v1/auth")
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
        if (authentication != null && !(authentication instanceof UsernamePasswordAuthenticationToken) && authentication.isAuthenticated()) {
            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwtUtil.generateToken((User) authentication.getPrincipal()));
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
        userAccount.setId(request.getEmail().toLowerCase());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setIsLock("N");

        userAccountRepository.save(userAccount);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
