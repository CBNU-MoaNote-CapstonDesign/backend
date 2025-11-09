package moanote.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;

/**
 * <pre>
 *  파일과 사용자 간의 다대다 관계이며, 파일에 대한 사용자 권한을 나타냅니다.
 *  사용자가 파일에 대해 가질 수 있는 권한은 READ, WRITE, DELETE 입니다.
 *
 * </pre>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@IdClass(FileUserDataId.class)
@Table(name = "file_user_data")
public class FileUserData {

  /**
   * <pre>
   *  사용자가 노트에 가질 수 있는 권한을 나타내는 enum 입니다.
   *  값의 순서가 권한의 대소관계를 나타냅니다.
   *  더 큰 권한은 더 작은 권한을 포함합니다.
   * </pre>
   */
  @Getter
  public enum Permission {
    READ("READ", 0),
    WRITE("WRITE", 1),
    DELETE("DELETE", 2),
    OWNER("OWNER", 3),
    NONE("NONE", -1);

    private final String permission;
    private final int value;

    Permission(String permission, int value) {
      this.permission = permission;
      this.value = value;
    }
  }

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id")
  @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
  private File file;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
  private UserData user;

  @Column(name = "permission", nullable = false)
  @Enumerated(EnumType.STRING)
  private Permission permission;
}