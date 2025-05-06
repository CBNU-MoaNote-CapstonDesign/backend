package moanote.backend.dto;

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

}
