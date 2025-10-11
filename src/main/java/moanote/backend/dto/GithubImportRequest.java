package moanote.backend.dto;

import java.util.UUID;

/**
 * <pre>
 *   GithubImportRequest 는 GitHub 저장소를 불러오기 위한 요청 정보를 표현합니다.
 * </pre>
 *
 * @param userId        파일을 소유할 사용자 식별자
 * @param repositoryUrl GitHub 저장소 URL
 * @param username      인증용 사용자 이름 (nullable)
 * @param token         인증용 Personal Access Token (nullable)
 */
public record GithubImportRequest(UUID userId, String repositoryUrl, String username, String token) {

  /**
   * @return 요청에 포함된 인증 정보를 {@link GithubCredentials} 형태로 반환합니다.
   */
  public GithubCredentials credentials() {
    return GithubCredentials.fromNullable(username, token);
  }
}
