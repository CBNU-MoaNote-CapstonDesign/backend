package moanote.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

/**
 * <pre>
 *  노트와 사용자 간의 다대다 관계이며, 노트에 대한 사용자 권한을 나타냅니다.
 *  사용자가 노트에 대해 가질 수 있는 권한은 READ, WRITE, DELETE 입니다.
 *
 * </pre>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@IdClass(NoteUserDataId.class)
@Table(name = "note_user_data")
public class NoteUserData {

  /**
   * <pre>
   *  사용자가 노트에 가질 수 있는 권한을 나타내는 enum 입니다.
   *  값의 순서가 권한의 대소관계를 나타냅니다.
   *  더 큰 권한은 더 작은 권한을 포함합니다.
   * </pre>
   */
  @Getter
  public enum Permission {
    READ("READ"),
    WRITE("WRITE"),
    DELETE("DELETE"),
    OWNER("OWNER");

    private final String permission;

    Permission(String permission) {
      this.permission = permission;
    }
  }

  @Id
  @ManyToOne
  @JoinColumn(name = "note_id")
  private Note note;

  @Id
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UserData user;

  @Column(name = "permission", nullable = false)
  @Enumerated(EnumType.STRING)
  private Permission permission;
}