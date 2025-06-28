package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.entity.Note;
import com.thanglv.sharedataapi.mapper.NoteMapper;
import com.thanglv.sharedataapi.repository.NoteRepository;
import com.thanglv.sharedataapi.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Override
    public ResponseEntity<NoteDto> createNote(CreateNoteRequest request) {
        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setCreatedAt(Instant.now());
        noteRepository.save(note);
        return ResponseEntity.ok(noteMapper.toDto(note));
    }

    @Override
    public ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size) {
        return null;
    }
}
