package moanote.backend.repository;

import moanote.backend.entity.FugueNode;
import moanote.backend.entity.TextNoteSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FugueNodeRepository extends JpaRepository<FugueNode, String> {

  List<FugueNode> findAllBySegment(TextNoteSegment segment);

  Optional<FugueNode> findBySegmentAndId(TextNoteSegment segment, String id);

  /**
   * <pre>
   * 비 루트 노드를 추가하는 메소드입니다.
   * 이유가 없다면 save 메소드를 직접 사용하여 비 루트 노드를 추가하는 대신 이 메소드를 사용해야 합니다.
   *
   * 이 메소드를 사용할 때에는 node 의 모든 필드가 채워져 있어야 합니다.
   * </pre>
   *
   * @param node 추가할 FugueNode 객체
   * @return 저장된 FugueNode 객체
   */
  default FugueNode insertNode(FugueNode node) {

    if (node.getId() == null) {
      throw new IllegalArgumentException("id must not be null");
    }

    if (node.getSegment() == null) {
      throw new IllegalArgumentException("segment must not be null");
    }

    if (node.getParent() == null) {
      throw new IllegalArgumentException("parent node must not be null");
    }

    if (node.getSide() == null) {
      throw new IllegalArgumentException("side must not be null");
    }

    if (node.getValue() == null) {
      throw new IllegalArgumentException("value must not be null");
    }

    return save(node);
  }

  /**
   * <pre>
   * 루트 노드를 생성하는 메소드입니다. key 가 아닌 필드는 무시되고 null 로 설정됩니다.
   * 이유가 없다면 save 메소드를 직접 사용하여 루트 노드를 생성하는 대신 이 메소드를 사용해야 합니다.
   *
   * 루트 노드는 항상 세그먼트 마다 하나만 존재해야 하고, caller 가 이를 체크해야 합니다.
   * </pre>
   *
   * @param segment 루트 노드가 속하는 TextNoteSegment 객체
   * @return 생성된 FugueNode 루트 노드 객체
   */
  default FugueNode createRootNode(TextNoteSegment segment) {
    FugueNode node = new FugueNode();

    node.setId("rt"); // 값에 의미는 없습니다.
    node.setSegment(segment);
    node.setParent(null);
    node.setSide(null);
    node.setValue(null);

    return save(node);
  }
}