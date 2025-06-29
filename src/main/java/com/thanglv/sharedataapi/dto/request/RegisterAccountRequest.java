package com.thanglv.sharedataapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAccountRequest {
    @NotBlank(message = "email can not be blank")
    @Email
    private String email;

    @NotBlank(message = "password can not be blank")
    private String password;
}
