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
//
//  @Autowired
//  private NoteService noteService;
//
//  @PostMapping("/create")
//  public Note createNote(@RequestBody UUID creatorId) {
//    return noteService.createNote(creatorId);
//  }
//
//  @GetMapping("/user/{userId}")
//  public List<Note> getNotesByUser(@PathVariable UUID userId) {
//    return noteService.getNotesByUserId(userId);
//  }
//
//  @GetMapping("/user/{userId}/owner")
//  public List<Note> getNotesByOwner(@PathVariable UUID userId) {
//    return noteService.getNotesByOwnerUserId(userId);
//  }
}