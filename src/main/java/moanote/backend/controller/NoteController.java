package moanote.backend.controller;

import moanote.backend.entity.Note;
import moanote.backend.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    @Autowired
    private NoteService noteService;

    @PostMapping("/create")
    public Note createNote() {
        return noteService.createNote();
    }

    @GetMapping("/user/{userId}")
    public List<Note> getNotesByUser(@PathVariable Long userId) {
        return noteService.getNotesByUserId(userId);
    }
}