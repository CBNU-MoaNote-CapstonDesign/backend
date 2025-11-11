package moanote.backend.domain;

import java.util.List;
import java.util.UUID;
import moanote.backend.entity.TextNoteSegment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link TextCollaborationSession} correctly initializes CRDT trees from plain-text segments.
 */
class TextCollaborationSessionTest {

  @Test
  void constructorBuildsTreesFromPlainTextSegments() {
    TextNoteSegment segment = new TextNoteSegment();
    segment.setId(UUID.randomUUID());
    segment.updateContent("Plain");

    TextCollaborationSession session = new TextCollaborationSession(List.of(segment));

    assertThat(String.join("", session.getSegment(segment.getId()).getOrderedElements())).isEqualTo("Plain");
    assertThat(session.getSegment(segment.getId()).getNodesDTO().getFirst().id()).isEqualTo("rt");
  }
}
