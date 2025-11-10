package moanote.backend.dto;

import java.util.UUID;

public record CaretDTO(UUID userId, String username, String color, int lineNumber, int columnNumber) {

}
