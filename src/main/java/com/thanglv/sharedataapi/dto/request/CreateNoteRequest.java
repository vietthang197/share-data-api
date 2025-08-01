package com.thanglv.sharedataapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateNoteRequest {

    @NotBlank(message = "title can not be blank")
    private String title;

    @NotBlank(message = "content can not be blank")
    private String content;
}
