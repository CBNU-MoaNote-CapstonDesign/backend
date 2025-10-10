package moanote.backend.dto;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import moanote.backend.entity.Note.CodeLanguage;

public record NoteDTO(
    FileDTO file,
    Map<UUID, SegmentType> segments,
    boolean sourceCode,
    Optional<CodeLanguage> codeLanguage) {

  public NoteDTO {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(segments, "segments");
    if (codeLanguage == null || !sourceCode) {
      codeLanguage = Optional.empty();
    }
  }
}
