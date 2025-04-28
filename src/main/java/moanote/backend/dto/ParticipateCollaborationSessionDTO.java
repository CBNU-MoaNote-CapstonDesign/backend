package moanote.backend.dto;

import java.util.UUID;

public record ParticipateCollaborationSessionDTO(
    UUID participantUserId,
    UUID noteId
) {

}