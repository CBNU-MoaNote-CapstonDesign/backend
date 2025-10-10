package moanote.backend.service;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.transaction.Transactional;
import moanote.backend.domain.CRDTFugueTreeNode;
import moanote.backend.domain.TextCollaborationSession;
import moanote.backend.domain.TextCollaborationSession.Participation;
import moanote.backend.dto.CRDTOperationDTO;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.NoteDTO;
import moanote.backend.dto.OperationType;
import moanote.backend.dto.SegmentType;
import moanote.backend.dto.TextEditParticipateDTO;
import moanote.backend.dto.TextSegmentDTO;
import moanote.backend.entity.FugueNode;
import moanote.backend.entity.Note;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileUserDataRepository;
import moanote.backend.repository.FugueNodeRepository;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Collaborative editing sessions 을 관리하는 서비스 클래스
 */
@Service
public class TextCollaborativeEditingService {

  final private Map<UUID, TextCollaborationSession> collaborationSessions;

  final private TextNoteSegmentRepository segmentRepository;

  final private UserDataRepository userDataRepository;

  final private NoteRepository noteRepository;

  final private FugueNodeRepository fugueNodeRepository;

  final private FileUserDataRepository fileUserDataRepository;

  @Autowired
  public TextCollaborativeEditingService(TextNoteSegmentRepository segmentRepository,
      UserDataRepository userDataRepository, NoteRepository noteRepository,
      FugueNodeRepository fugueNodeRepository, FileUserDataRepository fileUserDataRepository) {
    this.noteRepository = noteRepository;
    this.fileUserDataRepository = fileUserDataRepository;
    this.collaborationSessions = new ConcurrentHashMap<>();
    this.segmentRepository = segmentRepository;
    this.userDataRepository = userDataRepository;
    this.fugueNodeRepository = fugueNodeRepository;
  }

  @Transactional
  public List<Participation> getUsersInSession(UUID sessionId) {
    TextCollaborationSession session = collaborationSessions.get(sessionId);
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
   * @param noteId         협업 대상 노트 ID
   * @param participantUserId 세션 참여자
   * @return 협업 세션의 TextNoteSegment DTO 의 리스트
   * @see TextCollaborativeEditingService#doCreateSession(Note, UserData, UUID)
   */
  @Transactional
  public TextEditParticipateDTO participateSession(
      UUID participantUserId, UUID noteId) {
    Note note = noteRepository.getReferenceById(noteId);
    UserData participant = userDataRepository.findById(participantUserId).orElseThrow();

    FileDTO fileDTO = new FileDTO(note.getFile(),
        fileUserDataRepository.findOwnerByFile(note.getFile()).getUser());
    Map<UUID, SegmentType> uuidSegmentTypeMap = new HashMap<>();
    note.getSegments().forEach(segment -> {
      if (segmentRepository.findById(segment.getId()).isPresent()) {
        uuidSegmentTypeMap.put(segment.getId(), SegmentType.TEXT);
      } else {
        uuidSegmentTypeMap.put(segment.getId(), SegmentType.DIAGRAM);
      }
    });
    var segmentLists = doParticipateSession(note, participant, noteId);
    NoteDTO noteDTO = new NoteDTO(
        fileDTO,
        uuidSegmentTypeMap,
        note.getType() == Note.NoteType.CODE,
        Optional.ofNullable(note.getCodeLanguage()));
    return new TextEditParticipateDTO(noteDTO, segmentLists);
  }

  /**
   * <pre>
   *   세션 참가 요청을 실제로 처리하는 메소드
   *   세션 ID 는 Segment ID 와 동일하게 간주합니다.
   *   세션이 존재하지 않으면, 세션을 생성합니다.
   * </pre>
   *
   * @param note     협업 대상 노트
   * @param participant 협업 세션 참여자
   * @param sessionId   세션 ID
   * @return 협업 세션의 TextNoteSegment DTO 의 리스트
   * @implNote participateSession() 와 분리한 이유는 다른 Service, Repository 와의 의존성을 기능에서 분리하기 위함입니다.
   * @see TextCollaborativeEditingService#participateSession(UUID, UUID)
   */
  @Transactional
  protected List<TextSegmentDTO> doParticipateSession(Note note, UserData participant,
      UUID sessionId) {
    TextCollaborationSession session = collaborationSessions.get(sessionId);
    if (session == null) {
      session = doCreateSession(note, participant, sessionId);

    } else {
      session.addParticipant(participant);
    }
    List<TextSegmentDTO> segmentDTOList = new LinkedList<>();
    session.getSegmentTreeMap().forEach((segmentId, tree) -> {
      var fugueNodeDTOList =  tree.getNodesDTO();
      segmentDTOList.add(new TextSegmentDTO(segmentId, fugueNodeDTOList.getFirst(), fugueNodeDTOList));
    });
    return segmentDTOList;
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
   * @see TextCollaborativeEditingService#doParticipateSession(Note, UserData, UUID)
   */
  @Transactional
  protected TextCollaborationSession doCreateSession(Note note, UserData participant,
      UUID sessionId) {
    TextCollaborationSession session = new TextCollaborationSession(
        segmentRepository.findAllByNote(note));
    session.addParticipant(participant);
    collaborationSessions.put(sessionId, session);
    return session;
  }

  @Transactional
  public void editSegment(List<CRDTOperationDTO> operations, UUID segmentId, UUID sessionId) {
    var session = collaborationSessions.get(sessionId);
    if (session == null) {
      throw new NoSuchElementException("Session not found");
    }

    TextNoteSegment segment = segmentRepository.findById(segmentId).orElseThrow();

    operations.forEach(operation -> editSegment(operation, segment, session));
  }

  @Transactional
  public void editSegment(CRDTOperationDTO operation, TextNoteSegment segment,
      TextCollaborationSession session) {
    CRDTFugueTreeNode appliedNode = session.applyOperation(segment.getId(), operation);
    FugueNode node;
    if (operation.type() == OperationType.INSERT) {
      if (operation.parentId() == null) {
        throw new IllegalArgumentException();
      }
      node = new FugueNode();
      var parent = fugueNodeRepository.findBySegmentAndId(segment, operation.parentId()).orElseThrow();
      node.setSide(operation.side());
      node.setValue(operation.value());
      node.setId(operation.nodeId());
      parent.addChild(node);
      segment.addNode(node);
    } else {
      node = fugueNodeRepository.findBySegmentAndId(segment, operation.nodeId()).orElseThrow();
      node.setValue(appliedNode.getValue());
    }
  }
}
