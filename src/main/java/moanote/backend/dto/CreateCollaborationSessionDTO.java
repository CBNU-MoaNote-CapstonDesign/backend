package moanote.backend.dto;

public record CreateCollaborationSessionDTO(
    String sessionId,
    Long noteId,
    String sessionCreateUserId
) {

}