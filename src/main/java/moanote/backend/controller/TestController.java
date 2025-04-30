package moanote.backend.controller;

import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.UserDataRepository;
import moanote.backend.service.NoteService;
import moanote.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * DB를 초기화합니다.
 */

@RestController
@RequestMapping("/") // 엔드포인트 경로 정의
public class TestController {
  @Autowired
  NoteService noteService;

  @Autowired
  UserService userService;

  @GetMapping("/initDB")
  public String initDB() {
    try {
      UserData u1 = userService.createUser("kim","moanote1234");
      UserData u2 = userService.createUser("sa","moanote1234");
      UserData u3 = userService.createUser("son","moanote1234");

      UserData bot = userService.createUser("moa-bot-id","moanote1234");

      Note n1 = noteService.createNote(u1.getId());
      Note n2 = noteService.createNote(u2.getId());
      Note n3 = noteService.createNote(u3.getId());

      noteService.updateNote(n1.getId(),"노트1의 내용입니다.");
      noteService.updateNote(n1.getId(),"노트2의 내용입니다.");
      noteService.updateNote(n1.getId(),"노트3의 내용입니다.");

      return "<html><body><h1>DB 초기화 완료</h1></body></html>";
    } catch (Exception e) {
      return "<html><body><h1>오류 발생</h1></body></html>";
    }
  }
}