package moanote.backend.domain;

import moanote.backend.dto.CRDTOperationDTO;
import moanote.backend.dto.FugueNodeDTO;
import moanote.backend.entity.TextNoteSegment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * <pre>
 *   CRDTFugueTreeNode 를 Map<nodeId, CRDTFugueTreeNode> 으로 관리하는 객체입니다.
 *   CRDT 연산의 entry point 역할을 합니다.
 * </pre>
 */
public class CRDTFugueTree {

  /**
   * 데이터를 가지지 않고, tree 의 entry point 역할을 하는 root node 입니다.
   */
  private final CRDTFugueTreeNode root;
  private final Map<String, CRDTFugueTreeNode> nodes;

  CRDTFugueTree() {
    this.root = new CRDTFugueTreeNode();
    nodes = new ConcurrentHashMap<>();
    nodes.put(root.getNodeId(), root);
  }

  public CRDTFugueTree(TextNoteSegment segment) {
    nodes = new ConcurrentHashMap<>();
    AtomicReference<CRDTFugueTreeNode> root = new AtomicReference<>(null);
    segment.getNodes().forEach(node -> {
      nodes.put(node.getId(), new CRDTFugueTreeNode(node.getId(), node.getValue()));
      if (node.getParent() == null) {
        root.set(new CRDTFugueTreeNode(node.getId(), node.getValue()));
      }
    });
    this.root = new CRDTFugueTreeNode(root.get().getNodeId(), root.get().getValue());
  }

  public CRDTFugueTreeNode insert(CRDTOperationDTO operation) {
    if (!nodes.containsKey(operation.parentId())) {
      return null;
    }
    CRDTFugueTreeNode parentNode = nodes.get(operation.parentId());
    CRDTFugueTreeNode newNode = new CRDTFugueTreeNode(operation.nodeId(), operation.value());
    parentNode.addNode(operation.side(), newNode);
    return nodes.put(newNode.getNodeId(), newNode);
  }

  public CRDTFugueTreeNode delete(CRDTOperationDTO operation) {
    if (!nodes.containsKey(operation.nodeId())) {
      return null;
    }
    var nodeToTombstone = nodes.get(operation.nodeId());
    nodeToTombstone.remove();
    return nodeToTombstone;
  }

  /**
   * @deprecated 이 메소드는 아직 구현되지 않았습니다.
   * @param nodeId 업데이트 할 블록의 ID
   * @param value 업데이트 할 값
   * @throws UnsupportedOperationException 아직 구현되지 않음
   */
  public void update(final String nodeId, final String value) {
      throw new UnsupportedOperationException("update() is not implemented yet");
//    if (!nodes.containsKey(nodeId)) {
//      return;
//    }
//    nodes.get(nodeId).update(value);
  }

  public void traverseTree(Consumer<CRDTFugueTreeNode> operation) {
    Stack<CRDTFugueTreeNode> visitPath = new Stack<>();
    Stack<Iterator<CRDTFugueTreeNode>> lastVisitChildIterators = new Stack<>();
    Stack<CRDTFugueTreeNode.Side> childrenToVisit = new Stack<>();

    visitPath.push(root);
    lastVisitChildIterators.push(root.getLeftChildren().iterator());
    childrenToVisit.push(CRDTFugueTreeNode.Side.LEFT);

    while (!visitPath.isEmpty()) {
      CRDTFugueTreeNode currentNode = visitPath.peek();
      Iterator<CRDTFugueTreeNode> iterator = lastVisitChildIterators.peek();
      CRDTFugueTreeNode.Side side = childrenToVisit.peek();

      CRDTFugueTreeNode nextNode;
      if (iterator.hasNext()) {
        nextNode = iterator.next();
        visitPath.push(nextNode);
        lastVisitChildIterators.push(nextNode.getLeftChildren().iterator());
        childrenToVisit.push(CRDTFugueTreeNode.Side.LEFT);
      } else if (side == CRDTFugueTreeNode.Side.LEFT) {
        lastVisitChildIterators.pop();
        lastVisitChildIterators.push(currentNode.getRightChildren().iterator());
        childrenToVisit.pop();
        childrenToVisit.push(CRDTFugueTreeNode.Side.RIGHT);
        continue;
      }
      visitPath.pop();
      lastVisitChildIterators.pop();
      childrenToVisit.pop();

      operation.accept(currentNode);
    }
  }

  /**
   * 트리의 모든 노드를 반환합니다
   *
   * @return 중위 순회 순서로 정렬된 모든 노드
   */
  public ArrayList<String> getOrderedElements() {
    ArrayList<String> output = new ArrayList<>(nodes.size());
    traverseTree(node -> node.get().ifPresent(output::addLast));
    return output;
  }

  public List<FugueNodeDTO> getNodesDTO() {
    List<FugueNodeDTO> fugueNodeDTOList = new LinkedList<>();
    Queue<CRDTFugueTreeNode> visitQueue = new LinkedList<>();
    fugueNodeDTOList.add(new FugueNodeDTO(root.getNodeId(), null, root.getValue()));
    visitQueue.add(root);

    while (!visitQueue.isEmpty()) {
      var node = visitQueue.poll();
      node.getLeftChildren().forEach(nextNode -> {
        fugueNodeDTOList.add(
            new FugueNodeDTO(nextNode.getNodeId(), node.getNodeId(), nextNode.getValue()));
        visitQueue.add(nextNode);
      });
      node.getRightChildren().forEach(nextNode -> {
        fugueNodeDTOList.add(
            new FugueNodeDTO(nextNode.getNodeId(), node.getNodeId(), nextNode.getValue()));
        visitQueue.add(nextNode);
      });
    }

    return fugueNodeDTOList;
  }
}