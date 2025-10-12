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
}