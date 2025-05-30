package moanote.backend.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *   CRDTOrderTreeNode 를 Map<nodeId, CRDTOrderTreeNode> 으로 관리하는 객체입니다.
 *   CRDT 연산의 entry point 역할을 합니다.
 * </pre>
 */
public class CRDTOrderTree {

  /**
   * 데이터를 가지지 않고, tree 의 entry point 역할을 하는 root node 입니다.
   */
  private final CRDTOrderTreeNode root;
  private final Map<String, CRDTOrderTreeNode> nodes;

  CRDTOrderTree() {
    this.root = new CRDTOrderTreeNode();
    nodes = new ConcurrentHashMap<>();
    nodes.put(root.getNodeId(), root);
  }

  public void insert(final CRDTOrderTreeNode newNode, final String parentNodeId,
      final CRDTOrderTreeNode.Side side) {
    if (!nodes.containsKey(parentNodeId)) {
      return;
    }
    CRDTOrderTreeNode parentNode = nodes.get(parentNodeId);
    parentNode.addNode(side, newNode);
    nodes.put(newNode.getNodeId(), newNode);
  }

  public void delete(final String nodeId) {
    if (!nodes.containsKey(nodeId)) {
      return;
    }
    nodes.get(nodeId).remove();
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

  /**
   * 비재귀로 트리를 중위 순회(Inorder)하며 블록을 출력합니다.
   *
   * @return 문서의 내용을 순서대로 출력한 문자열
   */
  public ArrayList<String> getOrderedElements() {
    ArrayList<String> output = new ArrayList<>(nodes.size());

    Stack<CRDTOrderTreeNode> visitPath = new Stack<>();
    Stack<Iterator<CRDTOrderTreeNode>> lastVisitChildIterators = new Stack<>();
    Stack<CRDTOrderTreeNode.Side> childsToVisit = new Stack<>();

    visitPath.push(root);
    lastVisitChildIterators.push(root.getLeftChildren().iterator());
    childsToVisit.push(CRDTOrderTreeNode.Side.LEFT);

    while (!visitPath.isEmpty()) {
      CRDTOrderTreeNode currentNode = visitPath.peek();
      Iterator<CRDTOrderTreeNode> iterator = lastVisitChildIterators.peek();
      CRDTOrderTreeNode.Side side = childsToVisit.peek();

      CRDTOrderTreeNode nextNode;
      if (iterator.hasNext()) {
        nextNode = iterator.next();
        visitPath.push(nextNode);
        lastVisitChildIterators.push(nextNode.getLeftChildren().iterator());
        childsToVisit.push(CRDTOrderTreeNode.Side.LEFT);
      } else if (side == CRDTOrderTreeNode.Side.LEFT) {
        lastVisitChildIterators.pop();
        lastVisitChildIterators.push(currentNode.getRightChildren().iterator());
        childsToVisit.pop();
        childsToVisit.push(CRDTOrderTreeNode.Side.RIGHT);
        continue;
      }
      visitPath.pop();
      lastVisitChildIterators.pop();
      childsToVisit.pop();

      currentNode.get().ifPresent(output::addLast);
    }

    return output;
  }
}