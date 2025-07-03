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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final UserAccountRepository userAccountRepository;
    private final NoteContentRepository noteContentRepository;
    private final QrService qrService;

    @Value("${frontend_domain}")
    private String frontendDomain;

    @Override
    public ResponseEntity<NoteDto> createNote(CreateNoteRequest request) {
        PolicyFactory policy = Sanitizers.FORMATTING;
        Note note = new Note();
        note.setTitle(policy.sanitize(request.getTitle()));
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
        noteContent.setContent(policy.sanitize(request.getContent()));
        noteContent = noteContentRepository.save(noteContent);
        return ResponseEntity.ok(noteMapper.toDto(note));
    }

    @Override
    public ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size, String query) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            if (StringUtils.isNotEmpty(query)) {
                Page<NoteDto> noteDtoPage = noteRepository.findDtoByCreatedByAndTitleLike(userAccountOptional.get().getId(), query, PageRequest.of(page / size, size, Sort.by("createdAt").descending()));
                return ResponseEntity.ok(noteDtoPage);
            } else {
                Page<NoteDto> noteDtoPage = noteRepository.findDtoByCreatedBy(userAccountOptional.get().getId(), PageRequest.of(page / size, size, Sort.by("createdAt").descending()));
                return ResponseEntity.ok(noteDtoPage);
            }
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
            URI uri = new URI(frontendDomain);
            uri = uri.resolve("/note/").resolve(noteId);
            String shareLink = uri.toString();
            String base64Content = qrService.generateQRCodeImageBase64( shareLink, 200, 200);
            GenQrShareNoteResponse response = new GenQrShareNoteResponse();
            response.setQr(base64Content);
            response.setLink(shareLink);
            response.setMessage("OK");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
