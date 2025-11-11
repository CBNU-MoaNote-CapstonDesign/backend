package moanote.backend.dto;

import lombok.Getter;

@Getter
public enum SegmentType {
  TEXT("text", 0), DIAGRAM("diagram", 1);
  private final String name;
  private final Integer value;
  SegmentType(String name, Integer value) {
    this.name = name;
    this.value = value;
  }
}
