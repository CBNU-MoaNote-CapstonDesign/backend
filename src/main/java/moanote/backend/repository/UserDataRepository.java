package moanote.backend.repository;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserDataRepository extends JpaRepository<UserData, UUID> {
  default UserData create(String username, String password) {
    UserData userData = new UserData();
    userData.setUsername(username);
    userData.setPassword(password);
    userData.setId(UuidCreator.getTimeOrderedEpoch());
    return save(userData);
  }
  Optional<UserData> findByUsername(String username);
}