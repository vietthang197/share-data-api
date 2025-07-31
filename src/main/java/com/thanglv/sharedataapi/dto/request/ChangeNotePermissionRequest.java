package com.thanglv.sharedataapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeNotePermissionRequest {
    @NotBlank(message = "email can not be blank")
    private String email;
    @NotBlank(message = "permission can not be blank")
    @Pattern(regexp = "^(view|edit|revoke)$", message = "permission is invalid")
    private String permission;
}
