package moanote.backend.service;

import moanote.backend.config.GithubOAuthProperties;
import moanote.backend.dto.GithubOAuthAuthorizeResponse;
import moanote.backend.entity.UserData;
import moanote.backend.repository.UserDataRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <pre>
 *   GithubOAuthService 는 GitHub OAuth Authorization Code Flow 를 수행하기 위한 도우미 서비스를 제공합니다.
 *   OAuth state 정보는 데이터베이스 대신 메모리에 보관되며, 설정된 TTL 이 지나면 자동으로 무효화됩니다.
 * </pre>
 */
@Service
public class GithubOAuthService {

  private final GithubOAuthProperties properties;
  private final GithubTokenService githubTokenService;
  private final UserDataRepository userDataRepository;
  private final WebClient githubWebClient;
  private final ConcurrentMap<String, StoredState> stateStore = new ConcurrentHashMap<>();

  public GithubOAuthService(GithubOAuthProperties properties,
      GithubTokenService githubTokenService,
      UserDataRepository userDataRepository,
      WebClient.Builder webClientBuilder) {
    this.properties = properties;
    this.githubTokenService = githubTokenService;
    this.userDataRepository = userDataRepository;
    this.githubWebClient = webClientBuilder
        .baseUrl("https://github.com")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  /**
   * <pre>
   *   OAuth 인증을 시작하기 위한 GitHub Authorization URL 을 생성하고 state 값을 저장합니다.
   *   저장된 state 는 메모리에 보관되며 설정된 TTL 이 지나면 무효 처리됩니다.
   * </pre>
   *
   * @param userId OAuth 를 수행할 사용자
   * @return 생성된 Authorization URL 과 state 정보
   */
  public GithubOAuthAuthorizeResponse createAuthorizationUrl(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("userId must not be null");
    }

    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

    purgeExpiredStates();
    String state = UUID.randomUUID().toString();
    removeStatesForUser(user.getId());
    stateStore.put(state, StoredState.create(user.getId(), properties.getStateTtl()));

    String authorizationUrl = UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/authorize")
        .queryParam("client_id", properties.getClientId())
        .queryParam("redirect_uri", properties.getRedirectUri())
        .queryParam("scope", properties.getScope())
        .queryParam("state", state)
        .build(true)
        .toUriString();

    return new GithubOAuthAuthorizeResponse(authorizationUrl, state);
  }

  /**
   * <pre>
   *   GitHub 로부터 전달받은 Authorization Code 를 액세스 토큰으로 교환하고 사용자에게 저장합니다.
   *   교환이 완료되면 메모리에 저장된 state 정보를 즉시 제거합니다.
   * </pre>
   *
   * @param userId OAuth 를 수행한 사용자
   * @param code   GitHub 에서 전달한 Authorization Code
   * @param state  CSRF 방지를 위한 state 값
   */
  public void exchangeCode(UUID userId, String code, String state) {
    if (userId == null) {
      throw new IllegalArgumentException("userId must not be null");
    }
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("code must not be blank");
    }
    if (state == null || state.isBlank()) {
      throw new IllegalArgumentException("state must not be blank");
    }

    purgeExpiredStates();
    StoredState savedState = stateStore.get(state);
    if (savedState == null || savedState.isExpired()) {
      stateStore.remove(state);
      throw new IllegalArgumentException("Invalid or expired OAuth state");
    }

    if (!savedState.userId().equals(userId)) {
      throw new IllegalArgumentException("OAuth state does not belong to user: " + userId);
    }

    Map<String, Object> response = githubWebClient.post()
        .uri("/login/oauth/access_token")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of(
            "client_id", properties.getClientId(),
            "client_secret", properties.getClientSecret(),
            "code", code,
            "redirect_uri", properties.getRedirectUri(),
            "state", state
        ))
        .retrieve()
        .bodyToMono(Map.class)
        .blockOptional()
        .orElseThrow(() -> new IllegalStateException("GitHub OAuth response was empty"));

    String accessToken = (String) response.get("access_token");
    String tokenType = (String) response.getOrDefault("token_type", "bearer");
    String scope = (String) response.get("scope");

    if (accessToken == null || accessToken.isBlank()) {
      throw new IllegalStateException("GitHub OAuth response did not contain an access token");
    }

    githubTokenService.saveOrUpdateToken(userId, accessToken, tokenType, scope);
    stateStore.remove(state);
  }

  private void purgeExpiredStates() {
    Instant now = Instant.now();
    stateStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
  }

  private void removeStatesForUser(UUID userId) {
    stateStore.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
  }

  private record StoredState(UUID userId, Instant expiresAt) {

    static StoredState create(UUID userId, Duration ttl) {
      Instant expiresAt = Instant.now().plus(ttl);
      return new StoredState(userId, expiresAt);
    }

    boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }
}
