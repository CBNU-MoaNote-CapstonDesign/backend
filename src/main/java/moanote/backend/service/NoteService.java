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

  public Note createNote() {
    return noteRepository.createNote();
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

  /**
   * noteId 에 해당하는 Note 의 content 를 updatedContent 를 통해 업데이트합니다.
   *
   * @param noteId         업데이트할 Note 의 id
   * @param updatedContent content 의 수정 사항
   * @return 업데이트 대상이 된 Note entity
   * @throws NoSuchElementException noteId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public Note updateNote(Long noteId, String updatedContent) {
    return noteRepository.updateNote(noteId, updatedContent);
  }

  public List<Note> getNotesByUserId(Long userId) {
    return noteRepository.findByUserId(userId);
  }
}