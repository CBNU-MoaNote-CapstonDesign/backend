package moanote.backend.domain;

import moanote.backend.domain.CRDTFugueTreeNode.Side;
import moanote.backend.dto.CRDTOperationDTO;
import moanote.backend.dto.OperationType;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.*;

class CRDTOrderTreeTest {

  @Test
  void testGetOrderedElements() {
    CRDTFugueTree tree = new CRDTFugueTree();
    CRDTFugueTreeNode tail;
    tail = tree.insert(
        new CRDTOperationDTO(OperationType.INSERT, "1", "A", tree.getNodesDTO().getFirst().id(),
            Side.RIGHT, "user1"));

    Assert.isTrue(tail != null, "Tail should not be null after first insertion");
    for (int i = 2; i <= 5; i++) {
      tail = tree.insert(
          new CRDTOperationDTO(OperationType.INSERT, String.valueOf(i),
              String.valueOf((char) ('A' + i - 1)),
              tail.getNodeId(), Side.RIGHT, "user1"));
    }

    Assert.isTrue(String.join("", tree.getOrderedElements()).equals("ABCDE"),
        "Ordered elements should be ABCDE" + " but got " + String.join("", tree.getOrderedElements()));
  }
}