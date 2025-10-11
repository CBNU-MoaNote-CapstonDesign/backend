package moanote.backend.repository;

import moanote.backend.entity.GithubOAuthState;
import moanote.backend.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * <pre>
 *   GithubOAuthStateRepository 는 발급된 OAuth state 값과 사용자 매핑을 저장합니다.
 * </pre>
 */
public interface GithubOAuthStateRepository extends JpaRepository<GithubOAuthState, UUID> {

  Optional<GithubOAuthState> findByState(String state);

  void deleteByUser(UserData user);
}
