package com.thanglv.sharedataapi.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountPermissionDto {
    private String id;
    private String email;
    private String permission;
}
