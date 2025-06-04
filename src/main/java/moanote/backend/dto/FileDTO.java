package moanote.backend.dto;

import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;

import java.util.UUID;

public record FileDTO(String name, UUID id, FileType type, UUID dir) {

  public FileDTO(String name, UUID id, FileType type, UUID dir) {
    this.name = name;
    this.id = id;
    this.type = type;
    this.dir = dir;
  }

  public FileDTO(File file) {
    this(file.getName(), file.getId(), file.getType(), file.getDirectory() != null ? file.getDirectory().getId() : null);
  }
}
