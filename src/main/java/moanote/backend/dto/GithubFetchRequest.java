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
 * @param username       인증용 사용자 이름 (nullable)
 * @param token          인증용 Personal Access Token (nullable)
 */
public record GithubFetchRequest(UUID userId, String repositoryUrl, String branchName, String username,
                                 String token) {

  /**
   * @return 요청에 포함된 인증 정보
   */
  public GithubCredentials credentials() {
    return GithubCredentials.fromNullable(username, token);
  }
}
