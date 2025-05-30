package moanote.backend.dto;

import lombok.Getter;

/**
 * Enum representing the type of operation performed on a note for Op-based CRDT.
 */
@Getter
public enum OperationType {
  INSERT("INSERT"), REMOVE("REMOVE");
  private final String value;
  OperationType(String value) {
    this.value = value;
  }
}
