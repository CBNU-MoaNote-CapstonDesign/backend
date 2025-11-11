package moanote.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_data")
public class UserData {

  /**
   * UUIDv7
   */
  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;
}