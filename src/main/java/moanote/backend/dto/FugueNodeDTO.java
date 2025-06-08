package moanote.backend.dto;

import jakarta.annotation.Nullable;
import moanote.backend.entity.FugueNode;

public record FugueNodeDTO(String id, @Nullable String parentId, String value) {

  public FugueNodeDTO(FugueNode fugueNode) {
    this(fugueNode.getId(), fugueNode.getParent().getId(), fugueNode.getValue());
  }
}
