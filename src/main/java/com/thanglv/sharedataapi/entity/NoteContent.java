package com.thanglv.sharedataapi.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "note_content")
@Getter
@Setter
public class NoteContent {

    @Id
    private String id;

    @Indexed(unique = true)
    private String noteId;

    private String content;
}
