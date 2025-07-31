package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.services.AuthorizationService;
import com.thanglv.sharedataapi.util.OpenFGAConstant;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import dev.openfga.sdk.errors.FgaValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
class AuthorizationServiceImpl implements AuthorizationService {
    private final OpenFgaClient  openFgaClient;


    @Override
    public ClientWriteResponse write(ClientWriteRequest clientWriteRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return openFgaClient.write(clientWriteRequest).get();
    }

    @Override
    public ClientListObjectsResponse queryListObject(ClientListObjectsRequest clientListObjectsRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return openFgaClient.listObjects(clientListObjectsRequest).get();
    }

    @Override
    public ClientCheckResponse check(ClientCheckRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return openFgaClient.check(request).get();
    }

    @Override
    public ClientBatchCheckResponse batchCheck(ClientBatchCheckRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException, FgaValidationError {
        return openFgaClient.batchCheck(request).get();
    }

    @Override
    public ClientListUsersResponse queryListUsers(ClientListUsersRequest clientListUsersRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return openFgaClient.listUsers(clientListUsersRequest).get();
    }

    @Override
    public boolean hasPermission(String userPermission, String requiredPermission) {
        if (StringUtils.isEmpty(userPermission)) {
            return false;
        }
        switch (userPermission) {
            case OpenFGAConstant.RELATIONSHIP.owner -> {
                return true;
            }
            case OpenFGAConstant.RELATIONSHIP.edit -> {
                return !OpenFGAConstant.RELATIONSHIP.owner.equals(requiredPermission);
            }
            case OpenFGAConstant.RELATIONSHIP.view -> {
                return !OpenFGAConstant.RELATIONSHIP.edit.equals(requiredPermission) && !OpenFGAConstant.RELATIONSHIP.owner.equals(requiredPermission);
            }
            default -> {
                return false;
            }
        }
    }
}
