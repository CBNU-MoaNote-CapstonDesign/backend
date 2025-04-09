package moanote.backend.service;

import moanote.backend.domain.CollaborationSession;
import moanote.backend.dto.CreateCollaborationSessionDTO;
import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collaborative editing sessions 을 관리하는 서비스 클래스
 * TODO@ 실제로 DB 의 Note 내용을 변경하는 로직을 구현해야 함
 * TODO@ 세션이 종료되면 DB 에서 세션을 삭제하는 로직을 구현해야 함
 */
@Service
public class CollaborativeEditingService {

  final private Map<UUID, CollaborationSession> collaborationSessions;

  final private NoteService noteService;

  final private UserService userService;

  @Autowired
  public CollaborativeEditingService(NoteService noteService, UserService userService) {
    this.noteService = noteService;
    this.userService = userService;
    this.collaborationSessions = new ConcurrentHashMap<>();
  }

  public List<UserData> getUsersInSession(UUID sessionId) {
    CollaborationSession session = collaborationSessions.get(sessionId);
    if (session != null) {
      return session.getParticipants().values().stream().toList();
    }
    throw new IllegalArgumentException("Session not found");
  }

  /**
   * 협업 세션 생성의 entry point. 세션 ID 는 아마도 STOMP 에서 생성된 UUID 를 사용하게 될 것 같습니다.
   *
   * @param request 세션 생성 요청 DTO
   */
  public void createSession(CreateCollaborationSessionDTO request) {
    Note note = noteService.getNoteById(request.noteId());
    // TODO@ User 와 UserRepository 는 다른 기능 추가 중 변경 될 예정
    UserData participant = userService.findByUsername(request.sessionCreateUserId());
    doCreateSession(note, participant, request.sessionId());
  }

  /**
   * 세션을 실제로 생성하는 메소드. 기능과 DB 및 Service 와의 의존성을 분리하기 위해 만들어졌습니다.
   *
   * @param note        동시 수정 대상 노트
   * @param participant 동시 수정 세션 참여자
   * @param sessionId   세션 ID
   */
  public void doCreateSession(Note note, UserData participant, UUID sessionId) {
    CollaborationSession session = new CollaborationSession(note);
    session.addParticipant(participant);
    collaborationSessions.put(sessionId, session);
  }
}
