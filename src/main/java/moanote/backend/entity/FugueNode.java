package moanote.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moanote.backend.domain.CRDTOrderTreeNode;

/**
 * <pre>
 * Fugue CRDT 로직에서 각 노드를 표현하는 엔티티입니다.
 * 모든 text note segment 는 루트 노드를 항상 하나 가집니다.
 * 직접 FugueNodeRepository 의 save 를 통해 레코드를 생성할 수 없습니다.
 * 적절한 repository 메소드를 통해 생성하여 제약 조건이 지켜지는 지 확인할 수 있도록 해야합니다.
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@IdClass(FugueNodeId.class)
@Table(name = "fugue_node")
public class FugueNode {

  @Id
  @Column(name = "id")
  private String id;

  /**
   * 루트 노드의 경우에 TextNoteSegment 의 rootNode 와 순환 참조 관계를 가집니다.
   */
  @Id
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "segment_id")
  private TextNoteSegment segment;

  /**
   * 루트 노드의 parent 는 null 입니다.
   */
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "parent_id", nullable = true)
  private FugueNode parent;

  /**
   * 부모 노드의 왼족 자식 노드이면 LEFT, 오른쪽 자식 노드이면 RIGHT 입니다.
   */
  @Column(name = "side", nullable = true)
  @Enumerated(EnumType.STRING)
  private CRDTOrderTreeNode.Side side;

  /**
   * null 이면 tombstone 노드입니다.
   */
  @Column(name = "value", nullable = true)
  private String value;
}
