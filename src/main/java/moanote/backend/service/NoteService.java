package moanote.backend.service;

import moanote.backend.entity.Note;
import moanote.backend.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;

    public Note createNote(Note note) {
        return noteRepository.save(note);
    }
  public Note createNote() {
    return noteRepository.createNote();
  }

    public List<Note> getNotesByUserId(Long userId) {
        return noteRepository.findByUserId(userId);
    }
}