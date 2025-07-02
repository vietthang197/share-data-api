package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.GenQrShareNoteResponse;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.entity.Note;
import com.thanglv.sharedataapi.entity.NoteContent;
import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.mapper.NoteMapper;
import com.thanglv.sharedataapi.repository.NoteContentRepository;
import com.thanglv.sharedataapi.repository.NoteRepository;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
import com.thanglv.sharedataapi.services.NoteService;
import com.thanglv.sharedataapi.services.QrService;
import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final UserAccountRepository userAccountRepository;
    private final NoteContentRepository noteContentRepository;
    private final QrService qrService;

    @Override
    public ResponseEntity<NoteDto> createNote(CreateNoteRequest request) {
        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setCreatedAt(Instant.now());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            UserAccount userAccount = userAccountOptional.get();
            note.setCreatedBy(userAccount.getId());
        };
        note = noteRepository.save(note);

        NoteContent noteContent = new NoteContent();
        noteContent.setNoteId(note.getId());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES);
        String safeHtml = policy.sanitize(request.getContent());
        noteContent.setContent(safeHtml);
        noteContent = noteContentRepository.save(noteContent);
        return ResponseEntity.ok(noteMapper.toDto(note));
    }

    @Override
    public ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            Page<NoteDto> noteDtoPage = noteRepository.findDtoByCreatedBy(userAccountOptional.get().getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
            return ResponseEntity.ok(noteDtoPage);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<NoteDto> getContent(String noteId) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            NoteDto noteDto = noteMapper.toDto(note);
            Optional<NoteContent> noteContentOptional = noteContentRepository.findByNoteId(note.getId());
            noteContentOptional.ifPresent(noteContent -> noteDto.setContent(noteContent.getContent()));
            return ResponseEntity.ok(noteDto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<GenQrShareNoteResponse> genQrShareNote(String noteId) throws Exception {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isPresent()) {
            String base64Content = qrService.generateQRCodeImageBase64("http://localhost:4200/note/" + noteOptional.get().getId(), 200, 200);
            GenQrShareNoteResponse response = new GenQrShareNoteResponse();
            response.setQr(base64Content);
            response.setLink("http://localhost:4200/note/");
            response.setMessage("OK");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
