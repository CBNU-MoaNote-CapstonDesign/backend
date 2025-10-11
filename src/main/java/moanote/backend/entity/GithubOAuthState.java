package moanote.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * <pre>
 *   GithubOAuthState 는 OAuth Authorization Code Flow 에서 CSRF 방지를 위한 state 값을 저장합니다.
 * </pre>
 */
@Entity
@Table(name = "github_oauth_state")
@Getter
@Setter
@NoArgsConstructor
public class GithubOAuthState {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserData user;

  @Column(name = "state", nullable = false, unique = true, length = 128)
  private String state;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
