package moanote.backend.repository;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.entity.Note;
import moanote.backend.entity.TextNoteSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TextNoteSegmentRepository extends JpaRepository<TextNoteSegment, UUID>  {

  List<TextNoteSegment> findAllByNote(Note note);

  Optional<TextNoteSegment> findTextNoteSegmentById(UUID id);

  default TextNoteSegment create(TextNoteSegment segment) {
    segment.setId(UuidCreator.getTimeOrderedEpoch());
    return save(segment);
  }
}