package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.entity.Note;
import moanote.backend.entity.NoteUserData.Permission;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.NoteUserDataRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NoteService {

  private final NoteRepository noteRepository;

  private final UserDataRepository userDataRepository;

  private final NoteUserDataRepository noteUserDataRepository;

  public NoteService(NoteRepository noteRepository, UserDataRepository userDataRepository,
      NoteUserDataRepository noteUserDataRepository) {
    this.noteRepository = noteRepository;
    this.userDataRepository = userDataRepository;
    this.noteUserDataRepository = noteUserDataRepository;
  }

  /**
   * 새로운 Note 를 생성하고, 생성자에게 노트에 대한 OWNER 권한을 부여합니다.
   *
   * @param creatorId 생성자 id
   * @return 생성된 Note entity
   */
  @Transactional
  public Note createNote(UUID creatorId) {
    Note newNote = noteRepository.createNote();
    noteUserDataRepository.createNoteUserData(userDataRepository.findById(creatorId).orElseThrow(),
        newNote,
        Permission.OWNER);
    return newNote;
  }

  /**
   * noteId 에 해당하는 Note 검색
   *
   * @param noteId 찾을 Note 의 id
   * @return 찾아진 Note entity 객체
   * @throws NoSuchElementException noteId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public Note getNoteById(UUID noteId) {
    return noteRepository.findById(noteId).orElseThrow();
  }

  /**
   * noteId 에 해당하는 Note 의 content 를 updatedContent 를 통해 업데이트합니다.
   *
   * @param noteId         업데이트할 Note 의 id
   * @param updatedContent content 의 수정 사항
   * @return 업데이트 대상이 된 Note entity
   * @throws NoSuchElementException noteId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public Note updateNote(UUID noteId, String updatedContent) {
    return noteRepository.updateNote(noteId, updatedContent);
  }

  public List<Note> getNotesByOwnerUserId(UUID userId) {
    return noteRepository.findNotesByOwner(userId);
  }

  /**
   * note와 연관된 note_user_data를 모두 삭제한 뒤 note를 삭제합니다.
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