package com.thanglv.sharedataapi.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNoteRequest {
    private String title;
    private String content;
}
