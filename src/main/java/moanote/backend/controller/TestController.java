package moanote.backend.controller;

import moanote.backend.config.SecurityConfig;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.FileUserData.Permission;
import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import moanote.backend.service.FileService;
import moanote.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

/**
 * DB를 초기화합니다.
 */

@RestController
@RequestMapping("/api/dev") // 엔드포인트 경로 정의
public class TestController {
  @Autowired
  FileService fileService;

  @Autowired
  UserService userService;

  @Autowired
  private SecurityConfig securityConfig;
  private ArrayList<UserData> users = new ArrayList<>();
  private ArrayList<File> notes = new ArrayList<>();

  private final String AGENT_NAME;

  public TestController(@Value("${agent.name}") String AGENT_NAME) {
    this.AGENT_NAME = AGENT_NAME;
  }
  /**
   * DB에 테스트 데이터 주입하는 기능
   *
   * @return
   */
  @GetMapping("/initDB")
  public String initDB() {
    try {
      var s = securityConfig.passwordEncoder();
      String password = s.encode("1234");
      users.add(userService.createUser("kim", password));
      users.add(userService.createUser("sa", password));
      users.add(userService.createUser("son", password));

      users.add(userService.createUser(AGENT_NAME, password));

      return "<html><body><h1>테스트 데이터 주입 완료</h1></body></html>";
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "<html><body><h1>오류 발생</h1></body></html>";
    }
  }
}