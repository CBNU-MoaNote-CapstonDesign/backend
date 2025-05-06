package moanote.backend.controller;

import moanote.backend.dto.UserRegisterDTO;
import moanote.backend.entity.UserData;
import moanote.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;

  /**
   * 유저를 생성하는 API
   *
   * @param registerForm 유저 생성 요청
   * @return 생성된 유저 정보
   */
  @PostMapping("/register")
  public UserData registerUser(@RequestBody UserRegisterDTO registerForm) {
    // TODO@ 적절한 DTO 객체를 받도록 변경
    return userService.createUser(registerForm.username(), registerForm.password());
  }
}