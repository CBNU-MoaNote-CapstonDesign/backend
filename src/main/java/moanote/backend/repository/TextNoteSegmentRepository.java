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

  /**
   * 항상 FugueNodeRepository 의 createRootNode 메소드를 함께 사용해야 합니다.
   *
   * @param note segment 가 속한 Note
   * @return 새로운 TextNoteSegment 객체
   */
  default TextNoteSegment create(Note note) {
    TextNoteSegment textNoteSegment = new TextNoteSegment();

    textNoteSegment.setId(UuidCreator.getTimeOrderedEpoch());
    textNoteSegment.setNote(note);

    return save(textNoteSegment);
  }
}