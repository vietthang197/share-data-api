package com.thanglv.sharedataapi.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class AccountAccessNoteResponse extends BaseResponse {
    private List<UserAccountPermissionDto> accountList;
}
