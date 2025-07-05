package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.services.AuthorizationService;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
class AuthorizationServiceImpl implements AuthorizationService {
    private final OpenFgaClient  openFgaClient;


    @Override
    public ClientWriteResponse insert(ClientWriteRequest clientWriteRequest) throws FgaInvalidParameterException, ExecutionException, InterruptedException {

        return openFgaClient.write(clientWriteRequest).get();
    }

    @Override
    public ClientListObjectsResponse query() throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        var body = new ClientListObjectsRequest()
                .user("user:anne")
                .relation("view")
                .type("note");
        return openFgaClient.listObjects(body).get();
    }
}
