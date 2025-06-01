package moanote.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
   * 파일의 종류를 나타내는 열거형입니다.
   * This enum represents the type of file, either a document or a directory.
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
   * 파일의 이름
   * The name of the file or directory.
   */
  @Column(name = "name", nullable = false)
  private String name;

  /**
   * 파일의 종류를 나타내는 열거형입니다.
   * The type of the file, either a document or a directory.
   * DB에서는 INTEGER로 저장됩니다.
   */
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private FileType type;

  /**
   * 파일이 속한 디렉토리입니다.
   * This field is used to represent the parent directory of the file.
   *
   * 파일이 루트 디렉토리인 경우에만 null이 될 수 있습니다.
   */
  @ManyToOne
  @JoinColumn(name = "directory_id", nullable = true)
  private File directory;
}
