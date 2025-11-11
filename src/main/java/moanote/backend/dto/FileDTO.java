package moanote.backend.dto;

import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.UserData;
import org.apache.catalina.User;

import java.util.UUID;

public record FileDTO(String name, UUID id, FileType type, UUID dir, UserDataDTO owner,
                      boolean githubImported) {

  public FileDTO(String name, UUID id, FileType type, UUID dir, UserDataDTO owner,
      boolean githubImported) {
    this.name = name;
    this.id = id;
    this.type = type;
    this.dir = dir;
    this.owner = owner;
    this.githubImported = githubImported;
  }

  public FileDTO(File file, UserData owner) {
    this(file.getName(), file.getId(), file.getType(),
        file.getDirectory() != null ? file.getDirectory().getId() : null, new UserDataDTO(owner),
        file.isGithubImported());
  }
}
