package moanote.backend.repository;

import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import moanote.backend.entity.NoteUserData;
import moanote.backend.entity.NoteUserDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface NoteUserDataRepository extends JpaRepository<NoteUserData, NoteUserDataId> {

  default NoteUserData createNoteUserData(UserData user, Note note,
      NoteUserData.Permission permission) {
    NoteUserData noteUserData = new NoteUserData();
    noteUserData.setUser(user);
    noteUserData.setNote(note);
    noteUserData.setPermission(permission);
    return save(noteUserData);
  }

  void deleteAllByNoteId(UUID noteId);

  Optional<NoteUserData> findByNoteAndUser(Note note, UserData user);
}
