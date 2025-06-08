package moanote.backend.dto;

import java.util.Map;
import java.util.UUID;

public record NoteDTO(FileDTO file, Map<UUID, SegmentType> segments) {

}
