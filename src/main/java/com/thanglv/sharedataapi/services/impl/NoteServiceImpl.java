package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.BaseResponse;
import com.thanglv.sharedataapi.dto.response.GenQrShareNoteResponse;
import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.entity.Note;
import com.thanglv.sharedataapi.entity.NoteContent;
import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.mapper.NoteMapper;
import com.thanglv.sharedataapi.repository.NoteContentRepository;
import com.thanglv.sharedataapi.repository.NoteRepository;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
import com.thanglv.sharedataapi.services.AuthorizationService;
import com.thanglv.sharedataapi.services.NoteService;
import com.thanglv.sharedataapi.services.QrService;
import com.thanglv.sharedataapi.util.OpenFGAConstant;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final UserAccountRepository userAccountRepository;
    private final NoteContentRepository noteContentRepository;
    private final QrService qrService;
    private final AuthorizationService authorizationService;

    @Value("${frontend_domain}")
    private String frontendDomain;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<NoteDto> createNote(CreateNoteRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        log.info("@NoteServiceImpl createNote {}", request);
        var policy = Sanitizers.FORMATTING;
        var note = new Note();
        note.setTitle(policy.sanitize(request.getTitle()));
        note.setCreatedAt(Instant.now());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserAccount userAccount = userAccountOptional.get();
        note.setCreatedBy(userAccount.getId());
        note = noteRepository.save(note);

        var noteContent = new NoteContent();
        noteContent.setNoteId(note.getId());
        noteContent.setContent(policy.sanitize(request.getContent()));
        noteContent = noteContentRepository.save(noteContent);

        var setOwner = new ClientWriteRequest()
                .writes(List.of(
                        new ClientTupleKey()
                                .user(OpenFGAConstant.TYPE.user + ":" + userAccount.getId())
                                .relation(OpenFGAConstant.RELATIONSHIP.owner)
                                ._object(OpenFGAConstant.TYPE.note + ":" + note.getId())
                ));
        var clientResponse = authorizationService.insert(setOwner);
        if (clientResponse == null || !HttpStatusCode.valueOf(clientResponse.getStatusCode()).is2xxSuccessful()) {
            throw new RuntimeException("Failed to set owner note!");
        }

        return ResponseEntity.ok(noteMapper.toDto(note));
    }

    @Override
    public ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size, String query) {
        log.info("@NoteServiceImpl getNotes {} page:{} size:{} query:{}", page, size, query);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            if (StringUtils.isNotEmpty(query)) {
                var noteDtoPage = noteRepository.findDtoByCreatedByAndTitleLike(userAccountOptional.get().getId(), query, PageRequest.of(page / size, size, Sort.by("createdAt").descending()));
                return ResponseEntity.ok(noteDtoPage);
            } else {
                var noteDtoPage = noteRepository.findDtoByCreatedBy(userAccountOptional.get().getId(), PageRequest.of(page / size, size, Sort.by("createdAt").descending()));
                return ResponseEntity.ok(noteDtoPage);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<NoteDto> getContent(String noteId) {
        log.info("@NoteServiceImpl getContent noteId:{}", noteId);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            var userAccount = userAccountOptional.get();
            var noteOptional = noteRepository.findByIdAndCreatedBy(noteId, userAccount.getId());
            if (noteOptional.isPresent()) {
                var note = noteOptional.get();
                var noteDto = noteMapper.toDto(note);
                var noteContentOptional = noteContentRepository.findByNoteId(note.getId());
                noteContentOptional.ifPresent(noteContent -> noteDto.setContent(noteContent.getContent()));
                return ResponseEntity.ok(noteDto);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<GenQrShareNoteResponse> genQrShareNote(String noteId) throws Exception {
        log.info("@NoteServiceImpl genQrShareNote noteId:{}", noteId);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            var userAccount = userAccountOptional.get();
            var noteOptional = noteRepository.findByIdAndCreatedBy(noteId, userAccount.getId());
            if (noteOptional.isPresent()) {
                var uri = new URI(frontendDomain);
                uri = uri.resolve("/note/").resolve(noteId);
                String shareLink = uri.toString();
                String base64Content = qrService.generateQRCodeImageBase64(shareLink, 200, 200);
                GenQrShareNoteResponse response = new GenQrShareNoteResponse();
                response.setQr(base64Content);
                response.setLink(shareLink);
                response.setMessage("OK");
                response.setStatus(HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<BaseResponse> deleteNote(String noteId) {
        log.info("@NoteServiceImpl deleteNote noteId:{}", noteId);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            var userAccount = userAccountOptional.get();
            var noteOptional = noteRepository.findByIdAndCreatedBy(noteId, userAccount.getId());
            if (noteOptional.isPresent()) {
                var note = noteOptional.get();
                noteRepository.delete(note);
                var response = new BaseResponse();
                response.setStatus(HttpStatus.OK.value());
                response.setMessage("OK");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    public ResponseEntity<NoteDto> updateNote(String noteId, CreateNoteRequest request) {
        log.info("@NoteServiceImpl updateNote noteId:{}", noteId);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isPresent()) {
            var userAccount = userAccountOptional.get();
            var noteOptional = noteRepository.findByIdAndCreatedBy(noteId, userAccount.getId());
            if (noteOptional.isPresent()) {
                var note = noteOptional.get();
                note.setTitle(request.getTitle());
                note.setUpdatedBy(userAccount.getId());
                note.setUpdatedAt(Instant.now());
                noteRepository.save(note);
                var noteContentOptional = noteContentRepository.findByNoteId(note.getId());
                noteContentOptional.ifPresent(noteContent -> {
                    noteContent.setContent(request.getContent());
                    noteContentRepository.save(noteContent);
                });
                var noteDto = noteMapper.toDto(note);
                noteDto.setContent(request.getContent());
                return ResponseEntity.ok(noteDto);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
