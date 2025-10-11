package moanote.backend.service;

import moanote.backend.config.GithubOAuthProperties;
import moanote.backend.dto.GithubCredentials;
import moanote.backend.entity.UserData;
import moanote.backend.repository.UserDataRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <pre>
 *   GithubTokenService 는 사용자별로 발급된 GitHub OAuth 토큰을 메모리에 저장하고 조회합니다.
 *   토큰은 구성된 TTL 이 지나면 자동으로 만료되어 제거됩니다.
 * </pre>
 */
@Service
public class GithubTokenService {

  private final GithubOAuthProperties githubOAuthProperties;
  private final UserDataRepository userDataRepository;
  private final ConcurrentMap<UUID, StoredToken> tokenStore = new ConcurrentHashMap<>();

  public GithubTokenService(GithubOAuthProperties githubOAuthProperties, UserDataRepository userDataRepository) {
    this.githubOAuthProperties = githubOAuthProperties;
    this.userDataRepository = userDataRepository;
  }

  /**
   * <pre>
   *   사용자에게 저장된 GitHub 자격 증명을 조회합니다.
   * </pre>
   *
   * @param userId 토큰을 조회할 사용자 식별자
   * @return 저장된 자격 증명 또는 비어 있는 Optional
   */
  public Optional<GithubCredentials> findCredentials(UUID userId) {
    if (userId == null) {
      return Optional.empty();
    }

    purgeExpiredTokens();
    StoredToken storedToken = tokenStore.get(userId);
    if (storedToken == null || storedToken.isExpired()) {
      tokenStore.remove(userId);
      return Optional.empty();
    }

    return Optional.of(GithubCredentials.forOAuthToken(storedToken.accessToken()));
  }

  /**
   * <pre>
   *   토큰이 반드시 필요한 작업을 위해 사용자 자격 증명을 조회합니다.
   * </pre>
   *
   * @param userId 토큰이 필요한 사용자 식별자
   * @return 저장된 GitHub 자격 증명
   * @throws IllegalArgumentException 토큰이 존재하지 않는 경우
   */
  public GithubCredentials requireCredentials(UUID userId) {
    return findCredentials(userId)
        .orElseThrow(() -> new IllegalArgumentException("GitHub token not found for user: " + userId));
  }

  /**
   * <pre>
   *   OAuth 과정에서 발급된 액세스 토큰을 메모리에 저장하거나 갱신합니다.
   * </pre>
   *
   * @param userId      토큰을 저장할 사용자
   * @param accessToken 발급된 액세스 토큰
   * @param tokenType   토큰 타입 (예: {@code bearer})
   * @param scope       부여된 scope 문자열
   */
  public void saveOrUpdateToken(UUID userId, String accessToken, String tokenType, String scope) {
    if (userId == null) {
      throw new IllegalArgumentException("userId must not be null");
    }
    if (accessToken == null || accessToken.isBlank()) {
      throw new IllegalArgumentException("accessToken must not be blank");
    }
    if (tokenType == null || tokenType.isBlank()) {
      throw new IllegalArgumentException("tokenType must not be blank");
    }

    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

    tokenStore.put(user.getId(), StoredToken.create(accessToken, tokenType, scope, githubOAuthProperties.getTokenTtl()));
  }

  private void purgeExpiredTokens() {
    Instant now = Instant.now();
    tokenStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
  }

  private record StoredToken(String accessToken, String tokenType, String scope, Instant expiresAt) {

    static StoredToken create(String accessToken, String tokenType, String scope, Duration ttl) {
      Instant expiresAt = Instant.now().plus(ttl);
      return new StoredToken(accessToken, tokenType, scope, expiresAt);
    }

    boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }
}
