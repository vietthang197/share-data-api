package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.dto.request.ChangeNotePermissionRequest;
import com.thanglv.sharedataapi.dto.request.CreateNoteRequest;
import com.thanglv.sharedataapi.dto.response.*;
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
import dev.openfga.sdk.api.client.model.*;
import dev.openfga.sdk.api.model.FgaObject;
import dev.openfga.sdk.api.model.User;
import dev.openfga.sdk.api.model.UserTypeFilter;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import dev.openfga.sdk.errors.FgaValidationError;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    private final VirtualThreadExecutor virtualThreadExecutor;

    @Value("${frontend_domain}")
    private String frontendDomain;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<NoteDto> createNote(CreateNoteRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        log.info("@NoteServiceImpl createNote {}", request);
        var policy = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.TABLES).and(Sanitizers.STYLES);
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
                .writes(
                        List.of(
                                new ClientTupleKey()
                                        .user(OpenFGAConstant.TYPE.user + ":" + userAccount.getEmail())
                                        .relation(OpenFGAConstant.RELATIONSHIP.owner)
                                        ._object(OpenFGAConstant.TYPE.note + ":" + note.getId())
                        )
                );
        var clientResponse = authorizationService.write(setOwner);
        if (clientResponse == null || !HttpStatusCode.valueOf(clientResponse.getStatusCode()).is2xxSuccessful()) {
            throw new RuntimeException("Failed to set owner note!");
        }

        return ResponseEntity.ok(noteMapper.toDto(note));
    }

    @Override
    public ResponseEntity<Page<NoteDto>> getNotes(Integer page, Integer size, String query) {
        log.info("@NoteServiceImpl getNotes page:{} size:{} query:{}", page, size, query);
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
    public ResponseEntity<NoteDto> getContent(String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException, FgaValidationError {
        log.info("@NoteServiceImpl getContent noteId:{}", noteId);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var userAccount = userAccountOptional.get();
        var noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var note = noteOptional.get();
        // this is owner
        boolean hasViewPermission = userAccount.getId().equals(note.getCreatedBy());
        if (!hasViewPermission) {
            // check view permission
            var checkResponse = authorizationService.batchCheck(
                    new ClientBatchCheckRequest().checks(
                            List.of(
                                    new ClientBatchCheckItem()
                                            .user(OpenFGAConstant.TYPE.user + ":" + userAccount.getEmail())
                                            .relation(OpenFGAConstant.RELATIONSHIP.view)
                                            ._object(OpenFGAConstant.TYPE.note + ":" + note.getId()),
                                    new ClientBatchCheckItem()
                                            .user(OpenFGAConstant.TYPE.user + ":" + userAccount.getEmail())
                                            .relation(OpenFGAConstant.RELATIONSHIP.edit)
                                            ._object(OpenFGAConstant.TYPE.note + ":" + note.getId()),
                                    new ClientBatchCheckItem()
                                            .user(OpenFGAConstant.TYPE.user + ":" + userAccount.getEmail())
                                            .relation(OpenFGAConstant.RELATIONSHIP.owner)
                                            ._object(OpenFGAConstant.TYPE.note + ":" + note.getId())
                            )
                    )
            );
            if (checkResponse != null && CollectionUtils.isNotEmpty(checkResponse.getResult())) {
                List<ClientBatchCheckSingleResponse> checkList = checkResponse.getResult();
                hasViewPermission = checkList.stream().anyMatch(ClientBatchCheckSingleResponse::isAllowed);
            }
        }
        if (hasViewPermission) {
            var noteDto = noteMapper.toDto(note);
            var noteContentOptional = noteContentRepository.findByNoteId(note.getId());
            noteContentOptional.ifPresent(noteContent -> noteDto.setContent(noteContent.getContent()));
            return ResponseEntity.ok(noteDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
                noteContentRepository.deleteByNoteId(noteId);
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

    @Override
    public ResponseEntity<AccountAccessNoteResponse> getListAccountAccessNote(String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        try {
            log.info("@NoteServiceImpl getListAccountAccessNote noteId:{}", noteId);
            var auth = SecurityContextHolder.getContext().getAuthentication();
            var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
            if (userAccountOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            var userAccount = userAccountOptional.get();
            var noteOptional = noteRepository.findByIdAndCreatedBy(noteId, userAccount.getId());
            if (noteOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            var viewFeature = CompletableFuture.supplyAsync(() -> {
                try {
                    return getListUserByPermission(OpenFGAConstant.RELATIONSHIP.view, noteId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, virtualThreadExecutor);

            var editFeature = CompletableFuture.supplyAsync(() -> {
                try {
                    return getListUserByPermission(OpenFGAConstant.RELATIONSHIP.edit, noteId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, virtualThreadExecutor);

            var ownerFeature = CompletableFuture.supplyAsync(() -> {
                try {
                    return getListUserByPermission(OpenFGAConstant.RELATIONSHIP.owner, noteId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, virtualThreadExecutor);

            CompletableFuture.allOf(viewFeature, editFeature, ownerFeature).join();
            var response = new AccountAccessNoteResponse();

            var accountPermissionDtoList = new ArrayList<UserAccountPermissionDto>();
            accountPermissionDtoList.addAll(viewFeature.join());
            accountPermissionDtoList.addAll(editFeature.join());
            accountPermissionDtoList.addAll(ownerFeature.join());

            if (CollectionUtils.isNotEmpty(accountPermissionDtoList)) {
                accountPermissionDtoList
                        .sort((item1, item2) ->
                                OpenFGAConstant.RELATIONSHIP.owner.equals(item1.getPermission()) ? -1 :
                                        (OpenFGAConstant.RELATIONSHIP.owner.equals(item2.getPermission()) ? 1 : 0));
            }

            response.setAccountList(accountPermissionDtoList);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public List<UserAccountPermissionDto> getListUserByPermission(String permission, String noteId) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        log.info("@NoteServiceImpl getListUserByPermission permission:{} noteId:{}", permission, noteId);
        var accountPermissions = new ArrayList<UserAccountPermissionDto>();
        var clientListUsersRequest = new ClientListUsersRequest()
                ._object(new FgaObject().type(OpenFGAConstant.TYPE.note).id(noteId))
                .relation(permission)
                .userFilters(List.of(new UserTypeFilter().type(OpenFGAConstant.TYPE.user)));
        var clientListUsersResponse = authorizationService.queryListUsers(clientListUsersRequest);
        if (clientListUsersResponse == null || !HttpStatusCode.valueOf(clientListUsersResponse.getStatusCode()).is2xxSuccessful()) {
            throw new RuntimeException("Error call check permission failed");
        }
        var users = clientListUsersResponse.getUsers();
        if (CollectionUtils.isNotEmpty(users)) {
            Map<String, User> userMap = users.stream()
                    .filter(item -> item != null && item.getObject() != null)
                    .collect(Collectors.toMap(item -> item.getObject().getId(), value -> value, (oldValue, newValue) -> newValue));
            var userAccountDtoList = userAccountRepository.findAllByEmailIn(userMap.keySet()).stream().map(item -> {
                UserAccountPermissionDto userAccountPermissionDto = new UserAccountPermissionDto();
                userAccountPermissionDto.setEmail(item.getEmail());
                userAccountPermissionDto.setId(item.getId());
                userAccountPermissionDto.setPermission(permission);
                return userAccountPermissionDto;
            }).toList();
            accountPermissions.addAll(userAccountDtoList);
        }
        if (CollectionUtils.isNotEmpty(accountPermissions)) {
            return accountPermissions.stream()
                    .sorted((item1, item2) ->
                            OpenFGAConstant.RELATIONSHIP.owner.equals(item1.getPermission()) ? 1 :
                                    (OpenFGAConstant.RELATIONSHIP.owner.equals(item2.getPermission()) ? -1 : 0)).toList();
        }
        return accountPermissions;
    }

    @Override
    public ResponseEntity<BaseResponse> changeNotePermission(String noteId, ChangeNotePermissionRequest request) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userAccountOptional = userAccountRepository.findByEmail(auth.getName());
        if (userAccountOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BaseResponse response = new BaseResponse();
        var userAccount = userAccountOptional.get();
        var noteOptional = noteRepository.findByIdAndCreatedBy(noteId, userAccount.getId());

        if (noteOptional.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Note not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        var userShareOptional = userAccountRepository.findByEmail(request.getEmail());
        if (userShareOptional.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Email share not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        var userShare = userShareOptional.get();

        if (userAccountOptional.get().getId().equals(userShare.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"revoke".equalsIgnoreCase(request.getPermission())) {
            var checkResponse = authorizationService.check(
                    new ClientCheckRequest()
                            .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                            .relation(request.getPermission())
                            ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
            );
            if (checkResponse != null && HttpStatusCode.valueOf(checkResponse.getStatusCode()).is2xxSuccessful()
                    && Boolean.TRUE.equals(checkResponse.getAllowed())) {
                return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok());
            }
        }

        if (OpenFGAConstant.RELATIONSHIP.view.equals(request.getPermission())) {
            var checkResponse = authorizationService.check(
                    new ClientCheckRequest()
                            .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                            .relation(OpenFGAConstant.RELATIONSHIP.edit)
                            ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
            );
            if (checkResponse != null && HttpStatusCode.valueOf(checkResponse.getStatusCode()).is2xxSuccessful()
                    && Boolean.TRUE.equals(checkResponse.getAllowed())) {
                var removeEditPermission = new ClientWriteRequest().deletes(List.of(
                        new ClientTupleKey()
                                .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                                .relation(OpenFGAConstant.RELATIONSHIP.edit)
                                ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
                ));
                var removeResponse = authorizationService.write(removeEditPermission);
                if (removeResponse == null || !HttpStatusCode.valueOf(removeResponse.getStatusCode()).is2xxSuccessful()) {
                    throw new RuntimeException("Failed to remove permission note " + noteId + " to user " + userAccount.getId());
                }
            }
        } else if (OpenFGAConstant.RELATIONSHIP.edit.equals(request.getPermission())) {
            var checkResponse = authorizationService.check(
                    new ClientCheckRequest()
                            .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                            .relation(OpenFGAConstant.RELATIONSHIP.view)
                            ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
            );
            if (checkResponse != null && HttpStatusCode.valueOf(checkResponse.getStatusCode()).is2xxSuccessful()
                    && Boolean.TRUE.equals(checkResponse.getAllowed())) {
                var removeEditPermission = new ClientWriteRequest().deletes(List.of(
                        new ClientTupleKey()
                                .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                                .relation(OpenFGAConstant.RELATIONSHIP.view)
                                ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
                ));
                var removeResponse = authorizationService.write(removeEditPermission);
                if (removeResponse == null || !HttpStatusCode.valueOf(removeResponse.getStatusCode()).is2xxSuccessful()) {
                    throw new RuntimeException("Failed to remove permission note " + noteId + " to user " + userAccount.getId());
                }
            }
        } else if ("revoke".equalsIgnoreCase(request.getPermission())) {
            var checkResponse = authorizationService.check(
                    new ClientCheckRequest()
                            .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                            .relation(OpenFGAConstant.RELATIONSHIP.view)
                            ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
            );

            if (checkResponse == null  || !HttpStatusCode.valueOf(checkResponse.getStatusCode()).is2xxSuccessful()) {
                throw new RuntimeException("Failed to remove permission note " + noteId + " to user " + userAccount.getId());
            }
            if (Boolean.TRUE.equals(checkResponse.getAllowed())) {
                var removeViewPermission = new ClientWriteRequest().deletes(List.of(
                        new ClientTupleKey()
                                .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                                .relation(OpenFGAConstant.RELATIONSHIP.view)
                                ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
                ));
                var removeResponse = authorizationService.write(removeViewPermission);
                if (removeResponse == null || !HttpStatusCode.valueOf(removeResponse.getStatusCode()).is2xxSuccessful()) {
                    throw new RuntimeException("Failed to remove permission note " + noteId + " to user " + userAccount.getId());
                }
            }

            checkResponse = authorizationService.check(
                    new ClientCheckRequest()
                            .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                            .relation(OpenFGAConstant.RELATIONSHIP.edit)
                            ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
            );

            if (checkResponse == null  || !HttpStatusCode.valueOf(checkResponse.getStatusCode()).is2xxSuccessful()) {
                throw new RuntimeException("Failed to remove permission note " + noteId + " to user " + userAccount.getId());
            }
            if (Boolean.TRUE.equals(checkResponse.getAllowed())) {
                var removeEditPermission = new ClientWriteRequest().deletes(List.of(
                        new ClientTupleKey()
                                .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                                .relation(OpenFGAConstant.RELATIONSHIP.edit)
                                ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
                ));
                var removeResponse = authorizationService.write(removeEditPermission);
                if (removeResponse == null || !HttpStatusCode.valueOf(removeResponse.getStatusCode()).is2xxSuccessful()) {
                    throw new RuntimeException("Failed to remove permission note " + noteId + " to user " + userAccount.getId());
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok());
        }

        var setOwner = new ClientWriteRequest()
                .writes(
                        List.of(
                                new ClientTupleKey()
                                        .user(OpenFGAConstant.TYPE.user + ":" + userShare.getEmail())
                                        .relation(request.getPermission())
                                        ._object(OpenFGAConstant.TYPE.note + ":" + noteId)
                        )
                );
        var clientResponse = authorizationService.write(setOwner);
        if (clientResponse == null || !HttpStatusCode.valueOf(clientResponse.getStatusCode()).is2xxSuccessful()) {
            throw new RuntimeException("Failed to share note " + noteId + " to user " + userAccount.getId());
        }

        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok());
    }
}
