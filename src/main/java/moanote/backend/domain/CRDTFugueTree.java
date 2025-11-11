package moanote.backend.domain;

import moanote.backend.domain.CRDTFugueTreeNode.Side;
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
import java.util.function.Consumer;

/**
 * <pre>
 *   CRDTFugueTreeNode 를 Map<nodeId, CRDTFugueTreeNode> 으로 관리하는 객체입니다.
 *   CRDT 연산의 entry point 역할을 합니다.
 * </pre>
 */
public class CRDTFugueTree {

  private static final String ROOT_NODE_ID = "rt";

  /**
   * 데이터를 가지지 않고, tree 의 entry point 역할을 하는 root node 입니다.
   */
  private final CRDTFugueTreeNode root;
  private final Map<String, CRDTFugueTreeNode> nodes;

  CRDTFugueTree() {
    nodes = new ConcurrentHashMap<>();
    this.root = new CRDTFugueTreeNode(ROOT_NODE_ID, null);
    nodes.put(root.getNodeId(), root);
  }

  public CRDTFugueTree(TextNoteSegment segment) {
    this();
    populateFromPlainText(segment.getContent());
  }

  /**
   * Creates a new CRDT-based Fugue tree using a linear plain-text representation.
   *
   * @param plainText the plain-text content to translate into CRDT nodes. {@code null} is treated as an empty string.
   * @return a Fugue tree that contains the same textual information.
   */
  public static CRDTFugueTree fromPlainText(String plainText) {
    CRDTFugueTree tree = new CRDTFugueTree();
    tree.populateFromPlainText(plainText);
    return tree;
  }

  /**
   * Populates this tree instance using the provided plain-text data.
   *
   * @param plainText the source content that should be translated into CRDT nodes.
   */
  private void populateFromPlainText(String plainText) {
    String normalized = plainText == null ? "" : plainText;
    CRDTFugueTreeNode currentNode = root;
    for (int index = 0; index < normalized.length(); index++) {
      String nodeId = String.format("pl%08d", index);
      CRDTFugueTreeNode node = new CRDTFugueTreeNode(nodeId, String.valueOf(normalized.charAt(index)));
      currentNode.addNode(Side.RIGHT, node);
      nodes.put(nodeId, node);
      currentNode = node;
    }
  }

  public CRDTFugueTreeNode insert(CRDTOperationDTO operation) {
    if (!nodes.containsKey(operation.parentId())) {
      return null;
    }
    CRDTFugueTreeNode parentNode = nodes.get(operation.parentId());
    CRDTFugueTreeNode newNode = new CRDTFugueTreeNode(operation.nodeId(), operation.value());
    parentNode.addNode(operation.side(), newNode);
    nodes.put(newNode.getNodeId(), newNode);
    return newNode;
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
    operation.accept(root);
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
        operation.accept(nextNode);
        lastVisitChildIterators.push(nextNode.getLeftChildren().iterator());
        childrenToVisit.push(CRDTFugueTreeNode.Side.LEFT);
        continue;
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
    fugueNodeDTOList.add(new FugueNodeDTO(root.getNodeId(), null, root.getValue(), null));
    visitQueue.add(root);

    while (!visitQueue.isEmpty()) {
      var node = visitQueue.poll();
      node.getLeftChildren().forEach(nextNode -> {
        fugueNodeDTOList.add(
            new FugueNodeDTO(nextNode.getNodeId(), node.getNodeId(), nextNode.getValue(), Side.LEFT));
        visitQueue.add(nextNode);
      });
      node.getRightChildren().forEach(nextNode -> {
        fugueNodeDTOList.add(
            new FugueNodeDTO(nextNode.getNodeId(), node.getNodeId(), nextNode.getValue(), Side.RIGHT));
        visitQueue.add(nextNode);
      });
    }

    return fugueNodeDTOList;
  }
}