package com.thanglv.sharedataapi.services;

import com.thanglv.sharedataapi.dto.request.ChangeNotePermissionRequest;
import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import dev.openfga.sdk.errors.FgaValidationError;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface NoteService {
    ResponseEntity<NoteDto> createNote(CreateNoteRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size, String query);

    ResponseEntity<NoteDto> getContent(String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException, FgaValidationError;

    ResponseEntity<GenQrShareNoteResponse> genQrShareNote(String noteId) throws Exception;

    ResponseEntity<BaseResponse> deleteNote(String noteId);

    ResponseEntity<NoteDto> updateNote(String noteId, CreateNoteRequest request);

    ResponseEntity<AccountAccessNoteResponse> getListAccountAccessNote(String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    List<UserAccountPermissionDto> getListUserByPermission(String permission, String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ResponseEntity<BaseResponse> changeNotePermission(String noteId, ChangeNotePermissionRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException;
}
