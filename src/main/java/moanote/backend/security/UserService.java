package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.NoteUserDataRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

  @Autowired
  private UserDataRepository userDataRepository;

  @Autowired
  private NoteUserDataRepository noteUserDataRepository;

  @Autowired
  private NoteRepository noteRepository;

  public UserData createUser(String username, String password) {
    UserData userData = new UserData();
    userData.setUsername(username);
    userData.setPassword(password);
    userData.setId(UuidCreator.getTimeOrderedEpoch());
    return userDataRepository.save(userData);
  }

  public UserData findByUsername(String username) {
    return userDataRepository.findByUsername(username);
  }

  public Optional<UserData> findById(UUID id) {
    return userDataRepository.findById(id);
  }

  public boolean deleteByUsername(String username) {
    try {
      UserData user = findByUsername(username);
      userDataRepository.delete(user);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public boolean deleteById(UUID id) {
    try {
      Optional<UserData> user = findById(id);
      if (user.isPresent()) {
        userDataRepository.delete(user.get());
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * 유저를 포함해 유저와 관련된 모든 데이터를 지웁니다.
   * @param user
   * @return
   */
  public boolean delete(UserData user) {
    try {
      userDataRepository.delete(user);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * 전체 유저 레코드를 지웁니다.
   *
   * @return 성공 여부
   */
  public boolean deleteAll() {
    try {
      userDataRepository.deleteAll();
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}