package com.thanglv.sharedataapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken must not be blank")
    private String refreshToken;
}
