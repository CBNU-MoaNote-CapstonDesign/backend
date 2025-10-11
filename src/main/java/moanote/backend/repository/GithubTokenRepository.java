package moanote.backend.repository;

import moanote.backend.entity.GithubToken;
import moanote.backend.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * <pre>
 *   GithubTokenRepository 는 사용자별 GitHub 액세스 토큰을 관리합니다.
 * </pre>
 */
public interface GithubTokenRepository extends JpaRepository<GithubToken, UUID> {

  Optional<GithubToken> findByUser(UserData user);
}
