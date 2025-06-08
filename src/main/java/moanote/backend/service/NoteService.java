package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.entity.DiagramNoteSegment;
import moanote.backend.entity.File;
import moanote.backend.entity.Note;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.repository.DiagramNoteSegmentRepository;
import moanote.backend.repository.FugueNodeRepository;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NoteService {

  private final NoteRepository noteRepository;

  private final TextNoteSegmentRepository textNoteSegmentRepository;

  private final DiagramNoteSegmentRepository diagramNoteSegmentRepository;

  private final FugueNodeRepository fugueNodeRepository;

  @Autowired
  public NoteService(NoteRepository noteRepository,
      TextNoteSegmentRepository textNoteSegmentRepository,
      DiagramNoteSegmentRepository diagramNoteSegmentRepository,
      FugueNodeRepository fugueNodeRepository) {
    this.noteRepository = noteRepository;
    this.textNoteSegmentRepository = textNoteSegmentRepository;
    this.diagramNoteSegmentRepository = diagramNoteSegmentRepository;
    this.fugueNodeRepository = fugueNodeRepository;
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
    Note note = noteRepository.createNote(file);
    file.linkNote(note);
    return note;
  }

  @Transactional
  public TextNoteSegment createTextNoteSegment(UUID noteId) {
    var note = noteRepository.findNoteById(noteId).orElseThrow();
    var segment = new TextNoteSegment();
    note.addSegment(segment);

    var root = fugueNodeRepository.createRootNode(segment);
    segment.setRootNode(root);
    return textNoteSegmentRepository.create(segment);
  }

  @Transactional
  public DiagramNoteSegment createDiagramNoteSegment(UUID noteId) {
    var note = noteRepository.findNoteById(noteId).orElseThrow();
    var segment = new DiagramNoteSegment();
    note.addSegment(segment);
    return diagramNoteSegmentRepository.create(segment);
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