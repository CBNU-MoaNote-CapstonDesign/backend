package moanote.backend.repository;

import moanote.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(Long userId);
  default Note createNote() {
    Note note = new Note();
    note.setContent("");
    return save(note);
  }
}