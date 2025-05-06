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

  private boolean isLoggedIn(Authentication authentication) {
    return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails;
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
    if (!isLoggedIn(authentication))
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");

    // authentication의 getPrincipal()에서 필요한 정보만 뽑아서 UserDataDTO로 저장
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
    UserDataDTO data = new UserDataDTO(user.getId(), user.getUsername());
    System.out.println(user.getId());

    return ResponseEntity.ok(data);
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
  public ResponseEntity<?> getUser(Authentication authentication, @PathVariable UUID userId) {
    if (!isLoggedIn(authentication))
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");

    UserData user = userDataRepository.findById(userId).orElse(null);

    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    UserDataDTO data = new UserDataDTO(user.getId(), user.getUsername());

    return ResponseEntity.ok(data);
  }

}