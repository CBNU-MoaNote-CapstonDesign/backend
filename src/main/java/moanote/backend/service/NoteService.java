package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.entity.DiagramNoteSegment;
import moanote.backend.entity.File;
import moanote.backend.entity.Note;
import moanote.backend.entity.NoteUserData.Permission;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.repository.DiagramNoteSegmentRepository;
import moanote.backend.repository.FugueNodeRepository;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.NoteUserDataRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NoteService {

  private final NoteRepository noteRepository;

  private final UserDataRepository userDataRepository;

  private final NoteUserDataRepository noteUserDataRepository;

  private final TextNoteSegmentRepository textNoteSegmentRepository;

  private final DiagramNoteSegmentRepository diagramNoteSegmentRepository;

  private final FugueNodeRepository fugueNodeRepository;

  @Autowired
  public NoteService(NoteRepository noteRepository, UserDataRepository userDataRepository,
      NoteUserDataRepository noteUserDataRepository,
      TextNoteSegmentRepository textNoteSegmentRepository,
      DiagramNoteSegmentRepository diagramNoteSegmentRepository,
      FugueNodeRepository fugueNodeRepository) {
    this.noteRepository = noteRepository;
    this.userDataRepository = userDataRepository;
    this.noteUserDataRepository = noteUserDataRepository;
    this.textNoteSegmentRepository = textNoteSegmentRepository;
    this.diagramNoteSegmentRepository = diagramNoteSegmentRepository;
    this.fugueNodeRepository = fugueNodeRepository;
  }

  /**
   * 새로운 Note 를 생성하고, 생성자에게 노트에 대한 OWNER 권한을 부여합니다.
   * Document File 을 생성할 때 사용됩니다.
   *
   * @param creatorId 생성자 id
   * @return 생성된 Note entity
   */
  @Transactional
  public Note createNote(UUID creatorId, File file) {
    Note newNote = noteRepository.createNote(file);
    noteUserDataRepository.createNoteUserData(userDataRepository.findById(creatorId).orElseThrow(),
        newNote,
        Permission.OWNER);
    return newNote;
  }

  @Transactional
  public TextNoteSegment createTextNoteSegment(UUID noteId) {
    var segment = textNoteSegmentRepository.create(noteRepository.findNoteById(noteId)
        .orElseThrow());

    fugueNodeRepository.createRootNode(segment);
    return segment;
  }

  @Transactional
  public DiagramNoteSegment createDiagramNoteSegment(UUID noteId) {
    return diagramNoteSegmentRepository.create(noteRepository.findNoteById(noteId)
        .orElseThrow());
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

  /**
   * note와 연관된 note_user_data를 모두 삭제한 뒤 note를 삭제합니다.
   *
   * @param note 지울 note
   * @return 성공시 true, 실패시 false
   */
  public boolean delete(Note note) {
    try {
      noteUserDataRepository.deleteAllByNoteId(note.getId());
      //textChatMessageRepository.deleteAllByNoteId(note.getId());
      noteRepository.delete(note);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * 전체 note 레코드를 지웁니다.
   *
   * @return 성공시 true, 실패시 false
   */
  public boolean deleteAll() {
    try {
      noteRepository.deleteAll();
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}