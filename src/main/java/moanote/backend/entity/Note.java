package moanote.backend.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "note")
public class Note {
  /**
   * UUIDv7
   */
  @Id
  @Column(name = "id")
  private UUID id;

  @OneToOne(mappedBy = "note")
  private File file;

  @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<BaseNoteSegment> segments = new HashSet<>();

  public void addSegment(BaseNoteSegment segment) {
    segment.setNote(this);
    segments.add(segment);
  }

  public static Note create(File file) {
    Note note = new Note();
    note.setId(file.getId());
    file.linkNote(note);
    return note;
  }
}