package moanote.backend.dto;

import jakarta.annotation.Nullable;
import moanote.backend.domain.CRDTFugueTreeNode.Side;

/**
 * Represents a CRDT operation information.
 *
 * @param type     the type of the operation (INSERT | REMOVE)
 * @param nodeId   연산을 적용할 노드의 ID (INSERT의 경우 새로 생성되는 노드의 ID)
 * @param value    INSERT의 경우 새로 생성되는 노드의 값
 * @param parentId INSERT의 경우 새로 생성된 노드의 부모 ID
 */
public record CRDTOperationDTO(OperationType type, String nodeId, @Nullable String value,
                               @Nullable String parentId, @Nullable Side side, String byWho) {

}
