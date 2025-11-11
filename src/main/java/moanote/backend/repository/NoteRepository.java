package moanote.backend.repository;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

  Optional<Note> findNoteById(UUID noteId);

  default Note createNote(File file) {
    Note note = new Note();
    if (file.getType() != FileType.DOCUMENT) {
      throw new IllegalArgumentException("File type must be DOCUMENT");
    }
    note.setFile(file);
    return save(note);
  }
}