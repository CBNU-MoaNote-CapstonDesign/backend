package moanote.backend.dto;

import jakarta.annotation.Nullable;
import moanote.backend.domain.CRDTFugueTreeNode.Side;
public record FugueNodeDTO(String id, @Nullable String parentId, String value, Side side) {
}
