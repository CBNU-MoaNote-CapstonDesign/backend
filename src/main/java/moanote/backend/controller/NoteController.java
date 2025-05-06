package moanote.backend.controller;

import moanote.backend.entity.Note;
import moanote.backend.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

  @Autowired
  private NoteService noteService;

  /**
   * 유저가 노트를 생성하는 API
   *
   * @param creatorId userId 노트 생성자
   * @return 생성된 노트
   */
  @PostMapping("/create")
  public Note createNote(@RequestBody UUID creatorId) {
    return noteService.createNote(creatorId);
  }

  /**
   * userId 의 유저가 소유한 노트 리스트를 가져오는 API
   *
   * @param userId 노트 소유자 userId
   * @return 소유한 노트 리스트
   */
  @GetMapping("/user/{userId}")
  public List<Note> getNotesByOwner(@PathVariable UUID userId) {
    return noteService.getNotesByOwnerUserId(userId);
  }
}