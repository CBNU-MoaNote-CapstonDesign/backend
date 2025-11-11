package moanote.backend.dto;

import moanote.backend.entity.File.FileType;
import moanote.backend.entity.Note.CodeLanguage;

public record FileCreateDTO(String name, FileType type, Boolean isCode, CodeLanguage language) {
  public FileCreateDTO(String name, FileType type) {
    this(name, type, false, null);
  }

  public FileCreateDTO(String name, FileType type, Boolean isCode, CodeLanguage language) {
    this.name = name;
    this.type = type;
    this.isCode = (isCode != null) ? isCode : false;
    this.language = language;
  }
}
