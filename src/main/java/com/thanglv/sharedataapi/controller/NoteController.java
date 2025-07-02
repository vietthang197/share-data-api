package com.thanglv.sharedataapi.controller;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.GenQrShareNoteResponse;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@RequestBody CreateNoteRequest request) {
        return noteService.createNote(request);
    }

    @GetMapping
    public ResponseEntity<Page<NoteDto>> getNotes(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false, name = "q") String query) {
        return noteService.getNotes(page, size, query);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> getContent(@PathVariable String noteId) {
        return noteService.getContent(noteId);
    }

    @GetMapping("/gen-qr/{noteId}")
    public ResponseEntity<GenQrShareNoteResponse> genQrShareNote(@PathVariable String noteId) throws Exception {
        return noteService.genQrShareNote(noteId);
    }
}
