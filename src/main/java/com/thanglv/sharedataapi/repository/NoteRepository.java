package com.thanglv.sharedataapi.repository;

import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends MongoRepository<Note, String>, PagingAndSortingRepository<Note, String> {
    Page<NoteDto> findDtoByCreatedByAndTitleLike(String createdBy, String query, Pageable pageable);
    Page<NoteDto> findDtoByCreatedBy(String createdBy, Pageable pageable);
    Optional<Note> findByIdAndCreatedBy(String id, String createdBy);
}
