package moanote.backend.dto;

import java.util.List;
import java.util.UUID;

public record TextSegmentDTO(UUID id, FugueNodeDTO rootNode, List<FugueNodeDTO> nodes) {

}
