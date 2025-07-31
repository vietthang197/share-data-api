package com.thanglv.sharedataapi.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BaseResponse {
    protected int status;
    protected String message;

    public static BaseResponse ok() {
        BaseResponse response = new BaseResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("success");
        return response;
    }
}
