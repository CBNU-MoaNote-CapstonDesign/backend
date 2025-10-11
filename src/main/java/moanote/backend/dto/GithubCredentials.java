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
      return null;
    }
    return new GithubCredentials(username, token);
  }
}