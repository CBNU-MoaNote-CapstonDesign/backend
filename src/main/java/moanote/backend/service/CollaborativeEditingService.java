package moanote.backend.service;

import moanote.backend.domain.CollaborationSession;
import moanote.backend.domain.CollaborationSession.Participation;
import moanote.backend.domain.LWWNoteContent;
import moanote.backend.domain.LWWRegister;
import moanote.backend.dto.LWWStateDTO;
import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.UserDataRepository;
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

  final private NoteRepository noteRepository;

  final private UserDataRepository userDataRepository;

  @Autowired
  public CollaborativeEditingService(NoteService noteService, NoteRepository noteRepository,
      UserDataRepository userDataRepository) {
    this.noteService = noteService;
    this.noteRepository = noteRepository;
    this.collaborationSessions = new ConcurrentHashMap<>();
    this.userDataRepository = userDataRepository;
  }

  public List<Participation> getUsersInSession(UUID sessionId) {
    CollaborationSession session = collaborationSessions.get(sessionId);
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
   * @param noteId            협업 대상 노트 ID
   * @param participantUserId 세션 참여자
   * @return 세션 생성 후, LWWStateDTO 를 반환합니다.
   * @see CollaborativeEditingService#doCreateSession(Note, UserData, UUID)
   */
  public LWWStateDTO<LWWNoteContent> participateSession(
      UUID participantUserId, UUID noteId) {
    Note note = noteService.getNoteById(noteId);
    UserData participant = userDataRepository.findById(participantUserId).orElseThrow();

    return doParticipateSession(note, participant, noteId);
  }

  /**
   * <pre>
   *   세션 참가 요청을 실제로 처리하는 메소드
   *   세션 ID 는 Note ID 와 동일하게 간주합니다.
   *   세션이 존재하지 않으면, 세션을 생성합니다.
   * </pre>
   *
   * @param note        협업 대상 노트
   * @param participant 협업 세션 참여자
   * @param sessionId   세션 ID
   * @return 협업 세션의 LWWStateDTO
   * @implNote participateSession() 와 분리한 이유는 다른 Service, Repository 와의 의존성을 기능에서 분리하기 위함입니다.
   * @see CollaborativeEditingService#participateSession(UUID, UUID)
   */
  private LWWStateDTO<LWWNoteContent> doParticipateSession(Note note, UserData participant,
      UUID sessionId) {
    CollaborationSession session = collaborationSessions.get(sessionId);
    if (session == null) {
      return doCreateSession(note, participant, sessionId).getLWWStateDTO();
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
   * @param note        동시 수정 대상 노트
   * @param participant 동시 수정 세션 참여자
   * @param sessionId   세션 ID
   * @see CollaborativeEditingService#doParticipateSession(Note, UserData, UUID)
   */
  private CollaborationSession doCreateSession(Note note, UserData participant, UUID sessionId) {
    CollaborationSession session = new CollaborationSession(note);
    session.addParticipant(participant);
    collaborationSessions.put(sessionId, session);
    return session;
  }

  public void editNote(LWWStateDTO<LWWNoteContent> lwwStateDTO, UUID sessionId) {
    CollaborationSession session = collaborationSessions.get(sessionId);
    if (session == null) {
      throw new IllegalArgumentException("Session not found");
    }

    if (!session.applyEdit(
        new LWWRegister<>(lwwStateDTO.stateId(), lwwStateDTO.timeStamp(),
            lwwStateDTO.value()))) {
      return;
    }

    noteRepository.updateNote(session.noteId, lwwStateDTO.value().content());
  }
}
