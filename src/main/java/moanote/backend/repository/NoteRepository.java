package moanote.backend.repository;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import moanote.backend.entity.Note;
import moanote.backend.entity.NoteUserData;
import moanote.backend.entity.NoteUserData.Permission;
import moanote.backend.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

  @Query(value = """
      SELECT n
      FROM Note n
      JOIN NoteUserData nud ON n.id = nud.note.id
      WHERE nud.user.id = :owner
        AND nud.permission = 'OWNER'
      """)
  List<Note> findNotesByOwner(@Param("owner") UUID ownerUserId);

  @Query(value = """
      SELECT n
      FROM Note n
      JOIN NoteUserData nud ON n.id = nud.note.id
      WHERE nud.user.id = :user
      """)
  List<Note> findNotesByUser(@Param("user") UUID ownerUserId);

  Optional<Note> findById(UUID noteId);

  default Note createNote() {
    Note note = new Note();
    note.setContent("");
    note.setId(UuidCreator.getTimeOrderedEpoch());
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
  default Note updateNote(UUID noteId, String newContent) {
    Note note = findById(noteId).orElseThrow();
    note.setContent(newContent);
    return save(note);
  }
}