package com.thanglv.sharedataapi.controller;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
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
    public ResponseEntity<Page<NoteDto>> getNotes(@RequestParam Integer page, @RequestParam Integer size) {
        return noteService.getNotes(page, size);
    }
}
