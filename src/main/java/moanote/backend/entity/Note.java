package moanote.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
   * 노트의 종류를 나타내는 열거형입니다.
   */
  public enum NoteType {
    NORMAL("NORMAL", 0), CODE("CODE", 1);

    private final String name;
    private final int value;

    NoteType(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }

  public enum CodeLanguage {
    TEXT("TEXT", 0),
    JAVA("JAVA", 1),
    PYTHON("PYTHON", 2),
    CSHARP("CSHARP", 3),
    JAVASCRIPT("JAVASCRIPT", 4),
    JAVASCRIPT_JSX("JAVASCRIPT_JSX", 5),
    TYPESCRIPT("TYPESCRIPT", 6),
    TYPESCRIPT_JSX("TYPESCRIPT_JSX", 7),
    C("C", 8),
    CPP("CPP", 9);

    private final String name;
    private final int value;

    CodeLanguage(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }

  /**
   * UUIDv7
   */
  @Id
  @Column(name = "id")
  private UUID id;

  @OneToOne(mappedBy = "note")
  private File file;

  /**
   * <pre>
   * 노트의 종류를 나타내는 열거형입니다.
   * </pre>
   */
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NoteType type;

  @Column(name = "code_language")
  @Enumerated(EnumType.STRING)
  private CodeLanguage codeLanguage;

  @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<BaseNoteSegment> segments = new HashSet<>();

  public void addSegment(BaseNoteSegment segment) {
    segment.setNote(this);
    segments.add(segment);
  }

  public static Note create(File file) {
    Note note = new Note();
    note.setId(file.getId());
    note.setType(NoteType.NORMAL);
    note.setCodeLanguage(CodeLanguage.TEXT);
    file.linkNote(note);
    return note;
  }
}