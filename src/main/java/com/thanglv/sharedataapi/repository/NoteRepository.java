package com.thanglv.sharedataapi.repository;

import com.thanglv.sharedataapi.entity.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
}
