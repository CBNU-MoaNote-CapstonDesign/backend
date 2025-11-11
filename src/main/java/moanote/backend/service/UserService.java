package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.dto.UserDataDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.FileUserData.Permission;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.FileUserDataRepository;
import moanote.backend.repository.UserDataRepository;
import moanote.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

  @Autowired
  private UserDataRepository userDataRepository;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private FileUserDataRepository fileUserDataRepository;

  @Transactional
  public UserData createUser(String username, String password) {
    UserData userData = userDataRepository.create(username, password);
    File rootDirectory = fileRepository.createRootDirectory();
    fileUserDataRepository.createFileUserData(userData, rootDirectory, Permission.OWNER);
    return userData;
  }

  public UserData findByUsername(String username) {
    return userDataRepository.findByUsername(username).orElseThrow();
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

  private boolean isLoggedIn(Authentication authentication) {
    return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails;
  }

  public UserDataDTO getAuthenticationUser(Authentication authentication) {
    if (!isLoggedIn(authentication))
      return null;

    // authentication의 getPrincipal()에서 필요한 정보만 뽑아서 UserDataDTO로 반환
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    return new UserDataDTO(user.getId(), user.getUsername());
  }
}