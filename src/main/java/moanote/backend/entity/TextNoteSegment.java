package moanote.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

  /**
   * 루트 노드와 순환 참조 관계를 가집니다.
   */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "root_node_id", referencedColumnName = "id")
  private FugueNode rootNode;
}