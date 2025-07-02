package com.thanglv.sharedataapi.services;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.GenQrShareNoteResponse;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface NoteService {
    ResponseEntity<NoteDto> createNote(CreateNoteRequest request);

    ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size, String query);

    ResponseEntity<NoteDto> getContent(String noteId);

    ResponseEntity<GenQrShareNoteResponse> genQrShareNote(String noteId) throws Exception;
}
