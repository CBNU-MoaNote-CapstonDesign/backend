package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import moanote.backend.dto.AddSegmentDTO;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.NoteDTO;
import moanote.backend.dto.SegmentType;
import moanote.backend.entity.DiagramNoteSegment;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.FileUserData;
import moanote.backend.entity.FugueNode;
import moanote.backend.entity.Note;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.entity.UserData;
import moanote.backend.repository.DiagramNoteSegmentRepository;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.FileUserDataRepository;
import moanote.backend.repository.FugueNodeRepository;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NoteService {

  private final NoteRepository noteRepository;

  private final TextNoteSegmentRepository textNoteSegmentRepository;

  private final DiagramNoteSegmentRepository diagramNoteSegmentRepository;

  private final FugueNodeRepository fugueNodeRepository;

  private final EntityManager entityManager;

  private final UserDataRepository userDataRepository;

  private final FileRepository fileRepository;

  private final FileUserDataRepository fileUserDataRepository;

  @Autowired
  public NoteService(NoteRepository noteRepository,
      TextNoteSegmentRepository textNoteSegmentRepository,
      DiagramNoteSegmentRepository diagramNoteSegmentRepository,
      FugueNodeRepository fugueNodeRepository, EntityManager entityManager,
      UserDataRepository userDataRepository, FileRepository fileRepository,
      FileUserDataRepository fileUserDataRepository, FileUserDataRepository fileUserDataRepository1) {
    this.noteRepository = noteRepository;
    this.textNoteSegmentRepository = textNoteSegmentRepository;
    this.diagramNoteSegmentRepository = diagramNoteSegmentRepository;
    this.fugueNodeRepository = fugueNodeRepository;
    this.entityManager = entityManager;
    this.userDataRepository = userDataRepository;
    this.fileRepository = fileRepository;
    this.fileUserDataRepository = fileUserDataRepository1;
  }

  /**
   * 새로운 Note 를 생성합니다.
   * Document File 을 생성할 때 사용됩니다.
   *
   * @param creatorId 생성자 id
   * @return 생성된 Note entity
   */
  @Transactional
  public Note createNote(UUID creatorId, File file) {
    if (file.getType() != FileType.DOCUMENT) {
      throw new IllegalArgumentException("File type must be DOCUMENT");
    }
    return Note.create(file);
  }

  @Transactional
  public UUID createSegment(UUID noteId, UUID userId, AddSegmentDTO addSegmentDTO) {
    Note note = noteRepository.findById(noteId).orElseThrow();
    UserData userData = userDataRepository.findById(userId).orElseThrow();

    if (addSegmentDTO.type() == SegmentType.TEXT) {
      return createTextNoteSegment(noteId).getId();
    } else {
      return createDiagramNoteSegment(noteId).getId();
    }
  }

  @Transactional
  public TextNoteSegment createTextNoteSegment(UUID noteId) {
    var note = noteRepository.findNoteById(noteId).orElseThrow();
    var segment = new TextNoteSegment();
    segment.setId(UuidCreator.getTimeOrderedEpoch());
    note.addSegment(segment);
    entityManager.flush();

    FugueNode root = new FugueNode();
    root.setId("rt");
    root.setSegment(segment);
    root.setParent(null);
    root.setSide(null);
    root.setValue(null);
    segment.addNode(root);
    segment.setRootNode(root);
    return segment;
  }

  @Transactional
  public DiagramNoteSegment createDiagramNoteSegment(UUID noteId) {
    var note = noteRepository.findNoteById(noteId).orElseThrow();
    var segment = new DiagramNoteSegment();
    segment.setId(UuidCreator.getTimeOrderedEpoch());
    segment.setContent("");
    note.addSegment(segment);
    return segment;
  }

  @Transactional
  public NoteDTO getNoteMetadata(UUID noteId, UUID userId) {
    Note note = noteRepository.findById(noteId).orElseThrow();
    UserData userData = userDataRepository.findById(userId).orElseThrow();

    List<TextNoteSegment> textSegments = textNoteSegmentRepository.findAllByNote(note);
    List<DiagramNoteSegment> diagramSegments = diagramNoteSegmentRepository.findAllByNote(note);
    Map<UUID, SegmentType> segments = new HashMap<>();
    textSegments.forEach(segment -> segments.put(segment.getId(), SegmentType.TEXT));
    diagramSegments.forEach(segment -> segments.put(segment.getId(), SegmentType.DIAGRAM));
    return new NoteDTO(
        new FileDTO(note.getFile(), fileUserDataRepository.findOwnerByFile(note.getFile()).getUser()),
        segments
    );
  }

  /**
   * noteId 에 해당하는 Note 검색
   *
   * @param noteId 찾을 Note 의 id
   * @return 찾아진 Note entity 객체
   * @throws NoSuchElementException noteId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public Note getNoteById(UUID noteId) {
    return noteRepository.findNoteById(noteId).orElseThrow();
  }
}