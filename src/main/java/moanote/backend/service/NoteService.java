package moanote.backend.service;

import moanote.backend.entity.Note;
import moanote.backend.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

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
  /**
   * noteId 에 해당하는 Note 검색
   *
   * @param noteId 찾을 Note 의 id
   * @return 찾아진 Note entity 객체
   * @throws NoSuchElementException noteId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public Note getNoteById(Long noteId) {
    return noteRepository.findById(noteId).orElseThrow();
  }

}