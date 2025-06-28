package com.thanglv.sharedataapi.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAccountRequest {
    private String email;
    private String password;
}
