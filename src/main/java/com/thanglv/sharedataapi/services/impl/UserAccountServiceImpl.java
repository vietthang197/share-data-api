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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<BaseResponse> registerAccount(RegisterAccountRequest request) {
        Optional<UserAccount> accountOptional = userAccountRepository.findByEmail(request.getEmail().toLowerCase());
        BaseResponse response = new BaseResponse();
        if (accountOptional.isPresent()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(request.getEmail().toLowerCase());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        Optional<UserRole> userRole = userRoleRepository.findByRole(Constant.DEFAULT_USER_ROLE);
        userRole.ifPresent(role -> userAccount.setRoles(List.of(role)));
        userAccount.setIsLock(Constant.STR_N);
        userAccountRepository.save(userAccount);
        response.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<RefreshTokenResponse> refreshToken(RefreshTokenRequest request) {
        RefreshTokenResponse response = new RefreshTokenResponse();
        try {
            Jws<Claims> claimsJws = jwtUtil.parseToken(request.getRefreshToken());
            if (!Constant.TOKEN_TYPE_REFRESH.equals(claimsJws.getHeader().getType())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(claimsJws.getPayload().getSubject());
            if (userAccountOptional.isPresent()) {
                UserAccount userAccount = userAccountOptional.get();
                User user = new User(userAccount.getEmail(), userAccount.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
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
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwtUtil.generateAccessToken((User) authentication.getPrincipal()));
            response.setRefreshToken(jwtUtil.generateRefreshToken((User) authentication.getPrincipal()));
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
