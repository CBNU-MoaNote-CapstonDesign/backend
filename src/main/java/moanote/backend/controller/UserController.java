package moanote.backend.controller;

import moanote.backend.dto.UserDataDTO;
import moanote.backend.entity.UserData;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.UserDataRepository;
import moanote.backend.security.CustomUserDetails;
import moanote.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;

  @PostMapping("/register")
  public UserData registerUser(Map<String, String> userData) {
    // TODO@ 적절한 DTO 객체를 받도록 변경
    return userService.createUser(userData.get("username"), userData.get("password"));
  }

  /**
   * GET /api/users/me
   * 내 정보를 받아오는 API 입니다.
   *
   * @param authentication 유저의 http-only 토큰이 Spring Security에 의해 자동 주입됨
   * @return 로그인 되어있으면 유저의 UserDataDTO, 미로그인시 UNAUTHORIZED
   */
  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    UserDataDTO user = userService.getAuthenticationUser(authentication);

    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");
    }

    return ResponseEntity.ok(user);
  }

  @Autowired
  private UserDataRepository userDataRepository;

  /**
   * GET /api/users/{userId}
   * 해당하는 id의 유저 정보를 찾는 API 입니다.
   *
   * @param authentication 유저의 http-only 토큰이 Spring Security에 의해 자동 주입됨
   * @param userId 정보를 찾고자 하는 유저의 ID
   * @return 유저가 있으면 UserDataDTO, 없으면 NOT_FOUND, 미로그인시 UNAUTHORIZED
   */
  @GetMapping("/{userId}")
  public ResponseEntity<?> getUserById(Authentication authentication, @PathVariable UUID userId) {
    UserData user = userService.findById(userId).orElse(null);

    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    UserDataDTO data = new UserDataDTO(user.getId(), user.getUsername());

    return ResponseEntity.ok(data);
  }

  /**
   * GET /api/users/find/{userName}
   * 해당하는 name을 가진 유저 정보를 찾는 API 입니다.
   *
   * @param authentication 유저의 http-only 토큰이 Spring Security에 의해 자동 주입됨
   * @param userName 정보를 찾고자 하는 유저의 ID
   * @return 유저가 있으면 UserDataDTO, 없으면 NOT_FOUND, 미로그인시 UNAUTHORIZED
   */
  @GetMapping("/find/{userName}")
  public ResponseEntity<?> getUserByUsername(Authentication authentication, @PathVariable String userName) {
    UserData user = userService.findByUsername(userName);

    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    UserDataDTO data = new UserDataDTO(user.getId(), user.getUsername());

    return ResponseEntity.ok(data);
  }

}