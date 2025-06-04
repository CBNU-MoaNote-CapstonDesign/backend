package moanote.backend.dto;

import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import java.util.UUID;

public record FileDTO(String name, UUID id, FileType type, UUID dir) {

  public FileDTO(String name, UUID id, FileType type) {
    this(name, id, type, null);
  }

  public FileDTO(File file) {
    this(file.getName(), file.getId(), file.getType(),
        file.getDirectory() != null ? file.getDirectory().getId() : null);
  }
}
