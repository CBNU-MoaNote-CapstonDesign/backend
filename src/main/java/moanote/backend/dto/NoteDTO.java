package moanote.backend.dto;

import java.util.Map;
import java.util.UUID;
import moanote.backend.entity.Note.CodeLanguage;

public record NoteDTO(
    FileDTO file,
    Map<UUID, SegmentType> segments,
    boolean sourceCode,
    CodeLanguage codeLanguage) {

  public NoteDTO {
    if (!sourceCode) {
      codeLanguage = null;
    }
  }
}
