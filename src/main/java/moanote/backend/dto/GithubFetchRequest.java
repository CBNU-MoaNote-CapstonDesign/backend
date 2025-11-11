package moanote.backend.dto;

import java.util.UUID;

/**
 * <pre>
 *   GithubFetchRequest 는 원격 저장소의 변경 사항을 가져오기 위한 요청 정보입니다.
 * </pre>
 *
 * @param userId         로컬 저장소를 소유한 사용자 식별자
 * @param repositoryUrl  원격 저장소 URL
 * @param branchName     동기화할 브랜치 이름
 */
public record GithubFetchRequest(UUID userId, String repositoryUrl, String branchName) {

  /**
   * <pre>
   *   Fetch 요청은 더 이상 인증 정보를 포함하지 않으므로 항상 {@code null} 을 반환합니다.
   *   인증은 {@link moanote.backend.service.GithubTokenService} 가 관리하는 토큰을 사용합니다.
   * </pre>
   *
   * @return 항상 {@code null}
   */
  public GithubCredentials credentials() {
    return null;
  }
}
