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
   */
  default DiagramNoteSegment create(DiagramNoteSegment diagramNoteSegment) {
    diagramNoteSegment.setId(UuidCreator.getTimeOrderedEpoch());
    diagramNoteSegment.setContent("");
    return save(diagramNoteSegment);
  }
}
