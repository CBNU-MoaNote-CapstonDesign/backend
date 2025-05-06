package moanote.backend.controller;

import moanote.backend.config.SecurityConfig;
import moanote.backend.entity.Note;
import moanote.backend.entity.NoteUserData;
import moanote.backend.entity.UserData;
import moanote.backend.repository.NoteUserDataRepository;
import moanote.backend.service.NoteService;
import moanote.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

/**
 * DB를 초기화합니다.
 */

@RestController
@RequestMapping("/api/dev") // 엔드포인트 경로 정의
public class TestController {

  @Autowired
  NoteService noteService;

  @Autowired
  UserService userService;

  @Autowired
  private NoteUserDataRepository noteUserDataRepository;

  @Autowired
  private SecurityConfig securityConfig;
  private ArrayList<UserData> users = new ArrayList<>();
  private ArrayList<Note> notes = new ArrayList<>();

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


      for (int i = 0; i < users.size(); i++) {
        notes.add(noteService.createNote(users.get(i).getId()));
        noteService.updateNote(notes.get(i).getId(), "노트" + i);
        for (int j = 0; j < users.size(); j++) {
          if (i != j) {
            noteService.grantPermission(notes.get(i).getId(), users.get(j).getId(), NoteUserData.Permission.valueOf("WRITE"));
          }
        }
      }

      users.add(userService.createUser("moa-bot-id", password));

      return "<html><body><h1>테스트 데이터 주입 완료</h1></body></html>";
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "<html><body><h1>오류 발생</h1></body></html>";
    }
  }

  /**
   * DB에 주입한 테스트 데이터 삭제하는 기능
   *
   * @return 결과 HTML
   */
  @GetMapping("/cancelDB")
  public String cancelDB() {
    try {
      for (Note note : notes) {
        noteService.delete(note);
      }
      for (UserData user : users) {
        userService.delete(user);
      }
      return "<html><body><h1>테스트 데이터 삭제 완료</h1></body></html>";
    } catch (Exception e) {
      return "<html><body><h1>오류 발생</h1></body></html>";
    }
  }

  /**
   * DB에 저장된 모든 레코드를 지우는 기능
   *
   * @return 결과 HTML
   */
  @GetMapping("/resetDB")
  public String resetDB() {
    try {
      // TODO textChatMessageRepository 구현시 delete 실시
      // textChatMessageRepository.deleteAll();
      noteUserDataRepository.deleteAll();
      noteService.deleteAll();
      userService.deleteAll();
      return "<html><body><h1>DB 초기화 완료</h1></body></html>";
    } catch (Exception e) {
      return "<html><body><h1>오류 발생</h1></body></html>";
    }
  }
}