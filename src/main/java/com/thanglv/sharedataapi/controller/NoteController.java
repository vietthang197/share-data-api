package com.thanglv.sharedataapi.controller;

import com.thanglv.sharedataapi.dto.request.ChangeNotePermissionRequest;
import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.AccountAccessNoteResponse;
import com.thanglv.sharedataapi.dto.response.BaseResponse;
import com.thanglv.sharedataapi.dto.response.GenQrShareNoteResponse;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.services.NoteService;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import dev.openfga.sdk.errors.FgaValidationError;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@RequestBody CreateNoteRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return noteService.createNote(request);
    }

    @GetMapping
    public ResponseEntity<Page<NoteDto>> getNotes(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false, name = "q") String query) {
        return noteService.getNotes(page, size, query);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> getContent(@PathVariable String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException, FgaValidationError {
        return noteService.getContent(noteId);
    }

    @GetMapping("/gen-qr/{noteId}")
    public ResponseEntity<GenQrShareNoteResponse> genQrShareNote(@PathVariable String noteId) throws Exception {
        return noteService.genQrShareNote(noteId);
    }


    @DeleteMapping("/{noteId}")
    public ResponseEntity<BaseResponse> deleteNote(@PathVariable String noteId) {
        return noteService.deleteNote(noteId);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable String noteId, @RequestBody CreateNoteRequest request) {
        return noteService.updateNote(noteId, request);
    }

    @GetMapping("/access/{noteId}")
    public ResponseEntity<AccountAccessNoteResponse> getListAccountAccessNote(@PathVariable String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return noteService.getListAccountAccessNote(noteId);
    }

    @PostMapping("/change-permission/{noteId}")
    public ResponseEntity<BaseResponse> changeNotePermission(@PathVariable String noteId, @RequestBody @Valid ChangeNotePermissionRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return noteService.changeNotePermission(noteId, request);
    }
}
