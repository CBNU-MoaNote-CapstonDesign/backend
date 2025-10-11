package moanote.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * <pre>
 * 노트 문서와 디렉토리를 포함하는 파일을 표현하는 엔티티입니다.
 * This entity can represent both files and directories.
 * </pre>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "file")
public class File {

  /**
   * 파일의 종류를 나타내는 열거형입니다. This enum represents the type of file, either a document or a directory.
   */
  public enum FileType {
    DOCUMENT("DOCUMENT", 0), DIRECTORY("DIRECTORY", 1);

    private final String name;
    private final int value;

    FileType(String name, int value) {
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

  /**
   * <pre>
   * 파일의 이름
   * The name of the file or directory.
   * </pre>
   */
  @Column(name = "name", nullable = false)
  private String name;

  /**
   * <pre>
   * 파일의 종류를 나타내는 열거형입니다.
   * The type of the file, either a document or a directory.
   * DB에서는 INTEGER로 저장됩니다.
   * </pre>>
   */
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private FileType type;

  /**
   * <pre>
   * 파일이 속한 디렉토리입니다.
   * This field is used to represent the parent directory of the file.
   *
   * 파일이 루트 디렉토리인 경우에만 null이 될 수 있습니다.
   * </pre>
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "directory_id", nullable = true)
  private File directory;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "note_id")
  private Note note;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "directory", orphanRemoval = true)
  private Set<File> children = new HashSet<>();

  @Column(name = "github_imported", nullable = false)
  private boolean githubImported = false;

  public void addChild(File file) {
    file.setDirectory(this);
    children.add(file);
  }

  public void removeChild(File file) {
    if (!children.contains(file)) {
      throw new IllegalArgumentException(
          this.getId() + ":" + this.getName() + " is not actual parent directory of " + file.getId()
              + ":" + file.getName());
    }
    file.setDirectory(null);
    children.remove(file);
  }

  public void linkNote(Note note) {
    note.setFile(this);
    this.note = note;
  }
}
