package moanote.backend.dto;

import java.util.UUID;

/**
 * <pre>
 *   GithubImportRequest 는 GitHub 저장소를 불러오기 위한 요청 정보를 표현합니다.
 * </pre>
 *
 * @param userId        파일을 소유할 사용자 식별자
 * @param repositoryUrl GitHub 저장소 URL
 */
public record GithubImportRequest(UUID userId, String repositoryUrl) {

  /**
   * <pre>
   *   Import 요청은 더 이상 인증 정보를 직접 포함하지 않으므로 항상 {@code null} 을 반환합니다.
   *   실제 인증은 {@link moanote.backend.service.GithubTokenService} 에 저장된 토큰을 통해 처리됩니다.
   * </pre>
   *
   * @return 항상 {@code null}
   */
  public GithubCredentials credentials() {
    return null;
  }
}
