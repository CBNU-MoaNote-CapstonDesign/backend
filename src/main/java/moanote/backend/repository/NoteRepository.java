package moanote.backend.repository;

import moanote.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

  List<Note> findByUserId(Long userId);

  Optional<Note> findById(Long noteId);

  default Note createNote() {
    Note note = new Note();
    note.setContent("");
    return save(note);
  }

  /**
   * noteId 에 해당하는 Note 의 content 를 newContent 로 덮어씁니다.
   *
   * @param noteId     업데이트할 Note 의 id
   * @param newContent 저장할 content
   * @return 업데이트 대상이 된 Note entity
   * @throws NoSuchElementException noteId 에 해당하는 객체를 찾을 수 없는 경우
   */
  default Note updateNote(Long noteId, String newContent) {
    Note note = findById(noteId).orElseThrow();
    note.setContent(newContent);
    return save(note);
  }
}