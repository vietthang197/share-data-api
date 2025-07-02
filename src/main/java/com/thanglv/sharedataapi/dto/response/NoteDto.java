package com.thanglv.sharedataapi.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.PersistenceCreator;

import java.time.Instant;

@Getter
@Setter
public class NoteDto {
    private String id;

    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public NoteDto() {

    }

    @PersistenceCreator
    public NoteDto(String id, String title, String content, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
