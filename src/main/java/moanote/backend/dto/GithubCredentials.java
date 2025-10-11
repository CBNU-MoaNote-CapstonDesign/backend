package moanote.backend.dto;

import org.springframework.lang.Nullable;

/**
 * <pre>
 *   GithubCredentials 는 GitHub 인증 정보(username/token)를 보관하는 DTO 입니다.
 * </pre>
 *
 * @param username Personal Access Token 과 함께 사용할 사용자 이름 (nullable)
 * @param token    Personal Access Token (nullable)
 */
public record GithubCredentials(@Nullable String username, @Nullable String token) {

  private static final GithubCredentials ANONYMOUS = new GithubCredentials(null, null);

  /**
   * <pre>
   *   username 과 token 이 모두 null 혹은 blank 인 경우 null 을 반환하여 인증이 필요 없음을 표현합니다.
   * </pre>
   *
   * @param username Personal Access Token 과 함께 사용할 사용자 이름
   * @param token    Personal Access Token
   * @return 실제 인증 정보 또는 null
   */
  public static GithubCredentials fromNullable(String username, String token) {
    if ((username == null || username.isBlank()) && (token == null || token.isBlank())) {
      return ANONYMOUS;
    }
    return new GithubCredentials(username, token);
  }

  /**
   * <pre>
   *   GitHub OAuth 액세스 토큰은 사용자 이름 대신 고정된 {@code oauth2} 사용자 이름과 함께 Basic 인증으로 전달됩니다.
   * </pre>
   *
   * @param token GitHub OAuth 액세스 토큰
   * @return OAuth 토큰 기반의 인증 정보
   */
  public static GithubCredentials forOAuthToken(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("token must not be blank");
    }
    return new GithubCredentials("oauth2", token);
  }

  /**
   * @return 인증 정보가 존재하지 않음을 나타내는 상수
   */
  public static GithubCredentials anonymous() {
    return ANONYMOUS;
  }

  /**
   * @return 토큰이 실제로 존재하는지 여부
   */
  public boolean isPresent() {
    return token != null && !token.isBlank();
  }
}