package com.thanglv.sharedataapi.repository;

import com.thanglv.sharedataapi.entity.NoteContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteContentRepository extends MongoRepository<NoteContent, String> {
    Optional<NoteContent> findByNoteId(String noteId);
}
