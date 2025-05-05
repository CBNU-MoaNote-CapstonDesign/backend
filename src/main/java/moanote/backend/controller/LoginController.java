package moanote.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/login")
  public String login() {
    return "login"; // login.html 템플릿 반환
  }

  @GetMapping("/home")
  public String home() {
    return "home"; // 로그인 성공 후 보여줄 페이지
  }
}