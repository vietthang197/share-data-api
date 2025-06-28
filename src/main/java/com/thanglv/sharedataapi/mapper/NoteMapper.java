package com.thanglv.sharedataapi.mapper;

import com.thanglv.sharedataapi.dto.response.NoteDto;
import com.thanglv.sharedataapi.entity.Note;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    NoteDto toDto(Note note);
    List<NoteDto> toListDto(List<Note> notes);
}
