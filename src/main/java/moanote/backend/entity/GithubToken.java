package moanote.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * <pre>
 *   GithubToken 은 OAuth 과정을 통해 발급된 GitHub 액세스 토큰을 사용자별로 저장합니다.
 * </pre>
 */
@Entity
@Table(name = "github_token")
@Getter
@Setter
@NoArgsConstructor
public class GithubToken {

  @Id
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "user_id")
  private UserData user;

  @Column(name = "access_token", nullable = false, length = 255)
  private String accessToken;

  @Column(name = "token_type", nullable = false, length = 50)
  private String tokenType;

  @Column(name = "scope", length = 255)
  private String scope;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
