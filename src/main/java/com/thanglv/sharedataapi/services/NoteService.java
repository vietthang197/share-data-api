package com.thanglv.sharedataapi.services;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.BaseResponse;
import com.thanglv.sharedataapi.dto.response.GenQrShareNoteResponse;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

public interface NoteService {
    ResponseEntity<NoteDto> createNote(CreateNoteRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException;

    ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size, String query);

    ResponseEntity<NoteDto> getContent(String noteId);

    ResponseEntity<GenQrShareNoteResponse> genQrShareNote(String noteId) throws Exception;

    ResponseEntity<BaseResponse> deleteNote(String noteId);

    ResponseEntity<NoteDto> updateNote(String noteId, CreateNoteRequest request);
}
