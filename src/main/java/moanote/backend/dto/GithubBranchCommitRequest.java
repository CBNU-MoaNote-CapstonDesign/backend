package moanote.backend.dto;

import java.util.Map;
import java.util.UUID;

/**
 * <pre>
 *   GithubBranchCommitRequest 는 새로운 브랜치를 생성하고 커밋을 추가하기 위한 요청입니다.
 * </pre>
 *
 * @param userId         로컬 저장소를 소유한 사용자 식별자
 * @param repositoryUrl  원격 저장소 URL
 * @param baseBranch     새 브랜치 생성 시 기준이 되는 브랜치 이름
 * @param branchName     생성하거나 전환할 브랜치 이름
 * @param commitMessage  커밋 메시지
 * @param files          커밋에 포함될 파일 경로와 내용의 맵
 */
public record GithubBranchCommitRequest(UUID userId, String repositoryUrl, String baseBranch, String branchName,
                                        String commitMessage, Map<String, String> files) {

  /**
   * <pre>
   *   브랜치 생성/커밋 요청은 더 이상 인증 정보를 포함하지 않으므로 항상 {@code null} 을 반환합니다.
   *   인증은 {@link moanote.backend.service.GithubTokenService} 가 관리하는 토큰을 사용합니다.
   * </pre>
   *
   * @return 항상 {@code null}
   */
  public GithubCredentials credentials() {
    return null;
  }
}
