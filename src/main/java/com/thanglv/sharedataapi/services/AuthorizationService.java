package com.thanglv.sharedataapi.services;

import dev.openfga.sdk.api.client.model.ClientListObjectsResponse;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.client.model.ClientWriteResponse;
import dev.openfga.sdk.errors.FgaInvalidParameterException;

import java.util.concurrent.ExecutionException;

public interface AuthorizationService {
    ClientWriteResponse insert(ClientWriteRequest clientWriteRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ClientListObjectsResponse query() throws FgaInvalidParameterException, ExecutionException, InterruptedException;
}
