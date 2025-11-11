package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.domain.LWWCollaborationSession;
import moanote.backend.domain.LWWCollaborationSession.Participation;
import moanote.backend.domain.LWWNoteContent;
import moanote.backend.domain.LWWRegister;
import moanote.backend.dto.LWWStateDTO;
import moanote.backend.entity.DiagramNoteSegment;
import moanote.backend.entity.UserData;
import moanote.backend.repository.DiagramNoteSegmentRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collaborative editing sessions 을 관리하는 서비스 클래스
 * TODO@ 세션이 종료되면 세션을 삭제하는 로직을 구현해야 함
 */
@Transactional
@Service
public class LWWCollaborativeEditingService {

  final private Map<UUID, LWWCollaborationSession> collaborationSessions;

  final private DiagramNoteSegmentRepository segmentRepository;

  final private UserDataRepository userDataRepository;

  @Autowired
  public LWWCollaborativeEditingService(DiagramNoteSegmentRepository segmentRepository,
                                        UserDataRepository userDataRepository) {
    this.segmentRepository = segmentRepository;
    this.collaborationSessions = new ConcurrentHashMap<>();
    this.userDataRepository = userDataRepository;
  }

  public List<Participation> getUsersInSession(UUID sessionId) {
    LWWCollaborationSession session = collaborationSessions.get(sessionId);
    if (session != null) {
      return session.getParticipants().values().stream().toList();
    }
    throw new IllegalArgumentException("Session not found");
  }


  /**
   * <pre>
   *   세션 참여 요청을 처리하는 기능의 entry point
   *   세션이 존재하지 않으면, 세션을 생성합니다. See Also 를 참조하세요.
   * </pre>
   *
   * @param segmentId         협업 대상 세그먼트 ID
   * @param participantUserId 세션 참여자
   * @return 세션 생성 후, LWWStateDTO 를 반환합니다.
   * @see LWWCollaborativeEditingService#doCreateSession(DiagramNoteSegment, UserData, UUID)
   */
  public LWWStateDTO<LWWNoteContent> participateSession(
      UUID participantUserId, UUID segmentId) {
    DiagramNoteSegment segment = segmentRepository.getReferenceById(segmentId);
    UserData participant = userDataRepository.findById(participantUserId).orElseThrow();

    return doParticipateSession(segment, participant, segmentId);
  }

  /**
   * <pre>
   *   세션 참가 요청을 실제로 처리하는 메소드
   *   세션 ID 는 Segment ID 와 동일하게 간주합니다.
   *   세션이 존재하지 않으면, 세션을 생성합니다.
   * </pre>
   *
   * @param segment     협업 대상 세그먼트
   * @param participant 협업 세션 참여자
   * @param sessionId   세션 ID
   * @return 협업 세션의 LWWStateDTO
   * @implNote participateSession() 와 분리한 이유는 다른 Service, Repository 와의 의존성을 기능에서 분리하기 위함입니다.
   * @see LWWCollaborativeEditingService#participateSession(UUID, UUID)
   */
  protected LWWStateDTO<LWWNoteContent> doParticipateSession(DiagramNoteSegment segment, UserData participant,
      UUID sessionId) {
    LWWCollaborationSession session = collaborationSessions.get(sessionId);
    if (session == null) {
      return doCreateSession(segment, participant, sessionId).getLWWStateDTO();
    }
    session.addParticipant(participant);
    return session.getLWWStateDTO();
  }

  /**
   * <pre>
   *  협업 세션 생성의 entry point.
   *  Controller 에서 직접 이 메소드에 접근하는 대신, 세션 참가 요청을 통해서 접근합니다.
   * </pre>
   *
   * @param segment        동시 수정 대상 노트
   * @param participant 동시 수정 세션 참여자
   * @param sessionId   세션 ID
   * @see LWWCollaborativeEditingService#doParticipateSession(DiagramNoteSegment, UserData, UUID)
   */
  protected LWWCollaborationSession doCreateSession(DiagramNoteSegment segment, UserData participant, UUID sessionId) {
    LWWCollaborationSession session = new LWWCollaborationSession(segment);
    session.addParticipant(participant);
    collaborationSessions.put(sessionId, session);
    return session;
  }

  public void editSegment(LWWStateDTO<LWWNoteContent> lwwStateDTO, UUID sessionId) {
    LWWCollaborationSession session = collaborationSessions.get(sessionId);
    if (session == null) {
      throw new IllegalArgumentException("Session not found");
    }

    if (!session.applyEdit(
        new LWWRegister<>(lwwStateDTO.stateId(), lwwStateDTO.timeStamp(),
            lwwStateDTO.value()))) {
      return;
    }

    DiagramNoteSegment segment = segmentRepository.getReferenceById(session.segmentId);
    segment.setContent(lwwStateDTO.value().content());

    segmentRepository.save(segment);
  }
}
