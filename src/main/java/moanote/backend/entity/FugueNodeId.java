package moanote.backend.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class FugueNodeId implements Serializable {

  @EqualsAndHashCode.Include
  private String id;

  @EqualsAndHashCode.Include
  private TextNoteSegment segment;
}