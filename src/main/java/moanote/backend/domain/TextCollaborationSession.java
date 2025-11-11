package moanote.backend.domain;

import lombok.Getter;
import moanote.backend.dto.CRDTOperationDTO;
import moanote.backend.dto.OperationType;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.entity.UserData;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TextCollaborationSession {
  @Getter
  public static class Participation {

    private final UUID userId;
    private final String userName;
    private final String participationAt;

    public Participation(UUID userId, String userName) {
      this.userId = userId;
      this.userName = userName;
      this.participationAt = LocalDateTime.now().atZone(ZoneId.systemDefault())
          .withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
  }

  @Getter
  final private Map<UUID, CRDTFugueTree> segmentTreeMap;

  final private Map<UUID, Participation> participants;

  public TextCollaborationSession(List<TextNoteSegment> textSegments) {
    segmentTreeMap = new ConcurrentHashMap<>();
    textSegments.forEach(segment -> segmentTreeMap.put(segment.getId(),
        CRDTFugueTree.fromPlainText(segment.getContent())));
    participants = new ConcurrentHashMap<>();
  }

  public void addParticipant(UserData userData) {
    participants.put(userData.getId(), new Participation(userData.getId(), userData.getUsername()));
  }

  public void removeParticipant(UserData userData) {
    participants.remove(userData.getId());
  }

  public Map<UUID, Participation> getParticipants() {
    return Collections.unmodifiableMap(participants);
  }

  public int getParticipantsCount() {
    return participants.size();
  }

  public CRDTFugueTree getSegment(UUID segmentId) {
    return segmentTreeMap.get(segmentId);
  }

  public CRDTFugueTreeNode applyOperation(UUID segmentId, CRDTOperationDTO operation) {
    var tree = getSegment(segmentId);
    if (operation.type() == OperationType.INSERT) {
      return tree.insert(operation);
    } else {
      return tree.delete(operation);
    }
  }
}
