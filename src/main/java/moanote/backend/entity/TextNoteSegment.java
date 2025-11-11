package moanote.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "text_note_segment")
public class TextNoteSegment extends BaseNoteSegment {

  @Lob
  @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
  private String content = "";

  /**
   * Updates the persisted plain-text content that backs this text segment.
   *
   * @param content the new plain-text representation. {@code null} is treated as an empty string.
   */
  public void updateContent(String content) {
    this.content = content == null ? "" : content;
  }
}