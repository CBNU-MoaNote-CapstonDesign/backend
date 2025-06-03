package moanote.backend.repository;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.entity.Note;
import moanote.backend.entity.DiagramNoteSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiagramNoteSegmentRepository extends JpaRepository<DiagramNoteSegment, UUID> {

  List<DiagramNoteSegment> findAllByNote(Note note);

  Optional<DiagramNoteSegment> findDiagramNoteSegmentById(UUID id);

  /**
   * 다이어그램 노트 세그먼트를 생성합니다.
   *
   * @param note segment 가 속한 Note
   * @return 새로운 TextNoteSegment 객체
   */
  default DiagramNoteSegment create(Note note) {
    DiagramNoteSegment diagramNoteSegment = new DiagramNoteSegment();

    diagramNoteSegment.setId(UuidCreator.getTimeOrderedEpoch());
    diagramNoteSegment.setNote(note);
    diagramNoteSegment.setContent("");

    return save(diagramNoteSegment);
  }
}
