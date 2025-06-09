package moanote.backend.dto;

import moanote.backend.entity.UserData;
import java.util.UUID;

/**
 * <pre>
 *   UserDataDTO
 *   유저 정보 DTO
 * </pre>
 * @param id
 * @param name
 */
public record UserDataDTO(UUID id, String name) {

  public UserDataDTO(UserData user) {
    this(user.getId(), user.getUsername());
  }
}
