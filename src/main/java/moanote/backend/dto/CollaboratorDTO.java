package moanote.backend.dto;

import moanote.backend.entity.FileUserData;
import moanote.backend.entity.FileUserData.Permission;

public record CollaboratorDTO(UserDataDTO user, Permission permission) {

  public CollaboratorDTO(FileUserData fud) {
    this(new UserDataDTO(fud.getUser()), fud.getPermission());
  }
}
