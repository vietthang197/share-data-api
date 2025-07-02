package com.thanglv.sharedataapi.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenQrShareNoteResponse extends BaseResponse {
    private String qr;
    private String link;
}
