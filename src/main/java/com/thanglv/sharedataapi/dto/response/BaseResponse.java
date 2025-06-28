package com.thanglv.sharedataapi.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {
    protected int status;
    protected String message;
}
