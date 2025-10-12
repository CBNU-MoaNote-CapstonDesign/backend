package moanote.backend.repository;

import java.util.List;
import java.util.UUID;
import moanote.backend.entity.GithubImportedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubImportedRepositoryRepository extends JpaRepository<GithubImportedRepository, UUID> {

  /**
   * 지정한 사용자가 가져온 GitHub 저장소 메타데이터를 모두 조회합니다.
   *
   * @param userId 조회할 사용자 ID
   * @return 해당 사용자가 import 한 저장소 목록
   */
  List<GithubImportedRepository> findByUser_Id(UUID userId);

  /**
   * 지정한 사용자가 이미 동일한 저장소 URL 을 import 했는지 여부를 확인합니다.
   *
   * @param userId        저장소를 import 한 사용자 ID
   * @param repositoryUrl 중복 여부를 확인할 저장소 URL
   * @return 이미 import 한 경우 {@code true}
   */
  boolean existsByUser_IdAndRepositoryUrl(UUID userId, String repositoryUrl);
}