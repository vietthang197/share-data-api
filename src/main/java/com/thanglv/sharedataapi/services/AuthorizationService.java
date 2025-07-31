package com.thanglv.sharedataapi.services;

import dev.openfga.sdk.api.client.model.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import dev.openfga.sdk.errors.FgaValidationError;

import java.util.concurrent.ExecutionException;

public interface AuthorizationService {
    ClientWriteResponse write(ClientWriteRequest clientWriteRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ClientListObjectsResponse queryListObject(ClientListObjectsRequest clientListObjectsRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ClientCheckResponse check(ClientCheckRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ClientBatchCheckResponse batchCheck(ClientBatchCheckRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException, FgaValidationError;

    ClientListUsersResponse queryListUsers(ClientListUsersRequest clientListUsersRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    boolean hasPermission(String userPermission, String requiredPermission);
}
