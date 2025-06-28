package com.thanglv.sharedataapi.services;

import com.thanglv.sharedataapi.dto.request.LoginRequest;
import com.thanglv.sharedataapi.dto.request.RefreshTokenRequest;
import com.thanglv.sharedataapi.dto.request.RegisterAccountRequest;
import com.thanglv.sharedataapi.dto.response.BaseResponse;
import com.thanglv.sharedataapi.dto.response.LoginResponse;
import com.thanglv.sharedataapi.dto.response.RefreshTokenResponse;
import org.springframework.http.ResponseEntity;

public interface UserAccountService {
    ResponseEntity<BaseResponse> registerAccount(RegisterAccountRequest request);

    ResponseEntity<RefreshTokenResponse> refreshToken(RefreshTokenRequest request);

    ResponseEntity<LoginResponse> login(LoginRequest request);
}
