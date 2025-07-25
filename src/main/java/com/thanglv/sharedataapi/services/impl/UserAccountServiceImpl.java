package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.dto.request.LoginRequest;
import com.thanglv.sharedataapi.dto.request.RefreshTokenRequest;
import com.thanglv.sharedataapi.dto.request.RegisterAccountRequest;
import com.thanglv.sharedataapi.dto.response.BaseResponse;
import com.thanglv.sharedataapi.dto.response.LoginResponse;
import com.thanglv.sharedataapi.dto.response.RefreshTokenResponse;
import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.entity.UserRole;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
import com.thanglv.sharedataapi.repository.UserRoleRepository;
import com.thanglv.sharedataapi.services.UserAccountService;
import com.thanglv.sharedataapi.util.Constant;
import com.thanglv.sharedataapi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<BaseResponse> registerAccount(RegisterAccountRequest request) {
        var accountOptional = userAccountRepository.findByEmail(request.getEmail().toLowerCase());
        var response = new BaseResponse();
        if (accountOptional.isPresent()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Email already in use");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        var userAccount = new UserAccount();
        userAccount.setEmail(request.getEmail().toLowerCase());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        var userRole = userRoleRepository.findByRole(Constant.DEFAULT_USER_ROLE);
        userRole.ifPresent(role -> userAccount.setRoles(List.of(role)));
        userAccount.setIsLock(Constant.STR_N);
        userAccount.setCreatedAt(Instant.now());
        userAccountRepository.save(userAccount);
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Account created!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<RefreshTokenResponse> refreshToken(RefreshTokenRequest request) {
        var response = new RefreshTokenResponse();
        try {
            var claimsJws = jwtUtil.parseToken(request.getRefreshToken());
            if (!Constant.TOKEN_TYPE_REFRESH.equals(claimsJws.getHeader().getType())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            var userAccountOptional = userAccountRepository.findByEmail(claimsJws.getPayload().getSubject());
            if (userAccountOptional.isPresent()) {
                var userAccount = userAccountOptional.get();
                var user = new User(userAccount.getEmail(), userAccount.getPassword(),
                        userAccount.getRoles().stream()
                                .map(UserRole::getRole)
                                .map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
                response.setAccessToken(jwtUtil.generateAccessToken(user));
                response.setStatus(HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setMessage("Account does not exist");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (JwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        var response = new LoginResponse();
        try {
            var usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword());
            var authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
                response.setAccessToken(jwtUtil.generateAccessToken((User) authentication.getPrincipal()));
                response.setRefreshToken(jwtUtil.generateRefreshToken((User) authentication.getPrincipal()));
                response.setStatus(HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (BadCredentialsException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setMessage("Bad credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
