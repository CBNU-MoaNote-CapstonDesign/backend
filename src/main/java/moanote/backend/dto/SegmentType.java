package moanote.backend.dto;

import lombok.Getter;

@Getter
public enum SegmentType {
  TEXT("text"), DIAGRAM("diagram");
  private final String value;
  SegmentType(String value) {
    this.value = value;
  }
}
