package com.thanglv.sharedataapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenResponse extends BaseResponse {
    private String accessToken;
}
