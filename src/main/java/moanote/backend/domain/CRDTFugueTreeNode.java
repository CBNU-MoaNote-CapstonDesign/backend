package moanote.backend.domain;

import lombok.Getter;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

/**
 * <pre>
 *   Op-based Tree CRDT 로직을 구현한 객체입니다.
 *   특히 해당 객체는 Node 의 순서를 관리합니다.
 *   또한, 각 노드는 왼쪽 노드와 오른쪽 노드를 가지고 있습니다. 왼쪽 혹은 오른쪽을 Side 라고 부릅니다.
 *
 *   왼쪽 노드와 오른쪽 노드는 다수 존재할 수 있습니다. (LinkedList 로 관리)
 *   이는 동시 편집 시, 여러 노드가 동시에 추가되는 경우를 대비한 것입니다.
 *   같은 Side 에 있는 노드는 nodeId 에 의해 순서를 구분합니다.
 *
 *   연산은 addNode, removeNode, merge 만을 가집니다. (Commutative 한 연산)
 *
 *   제거된 Node 는 트리에서 완전히 제거되지 않고, value 를 null 로 변경합니다. (Tombstone)
 *
 *   tombstone 에 update 가 발생하면, 해당 연산은 무시됩니다.
 * </pre>
 */
@Getter
public class CRDTFugueTreeNode {

  public enum Side {
    LEFT, RIGHT
  }

  final private String nodeId;

  private String value;

  final private LinkedList<CRDTFugueTreeNode> leftChildren = new LinkedList<>();

  final private LinkedList<CRDTFugueTreeNode> rightChildren = new LinkedList<>();

  CRDTFugueTreeNode() {
    nodeId = UUID.randomUUID().toString();
    value = null;
  }

  CRDTFugueTreeNode(final String value) {
    this.nodeId = UUID.randomUUID().toString();
    this.value = value;
  }

  CRDTFugueTreeNode(final String nodeId, final String value) {
    this.nodeId = nodeId;
    this.value = value;
  }

  public void addNode(Side side, CRDTFugueTreeNode node) {
    LinkedList<CRDTFugueTreeNode> nodes = side == Side.LEFT ? leftChildren : rightChildren;
    synchronized (side == Side.LEFT ? leftChildren : rightChildren) {
      int insertIndex = 0;
      while (insertIndex < nodes.size()
          && nodes.get(insertIndex).getNodeId().compareTo(node.getNodeId()) < 0) {
        insertIndex++;
      }
      nodes.add(insertIndex, node);
    }
  }

  public void remove() {
    synchronized (this) {
      this.value = null;
    }
  }

  /**
   * @param value 노드의 값을 주어진 값으로 업데이트합니다.
   * @throws UnsupportedOperationException 아직 구현되지 않음
   * @deprecated 각 노드는 LWWRegister 가 아니므로 update() 메소드를 지원하지 않습니다.
   */
  public void update(String value) {
    throw new UnsupportedOperationException("update() is not supported yet.");
    // remove always wins against update
//    if (this.value.isEmpty()) {
//      return;
//    }
//    this.value = Optional.of(value);
  }

  public Optional<String> get() {
    synchronized (this) {
      return Optional.ofNullable(value);
    }
  }
}