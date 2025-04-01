package moanote.backend.domain;

import moanote.backend.entity.Note;
import moanote.backend.entity.User;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동일한 노트를 동시 편집하는 사용자 목록과 편집 사항을 관리하는 클래스입니다.
 */
public class CollaborationSession {

  /**
   * 동시 편집을 위한 LWWRegister 입니다. Content 를 관리합니다.
   *
   * @see LWWRegister
   */
  final private LWWRegister<LWWNoteContent> lwwRegister;

  /**
   * 동시 편집에 참여하는 사용자 목록입니다. ConcurrentHashMap 으로 Thread-safe 하게 관리합니다.
   */
  final private Map<Long, User> participants;

  public CollaborationSession(Note note) {
    this.lwwRegister = new LWWRegister<>("init", 0, new LWWNoteContent(note.getContent()));
    participants = new ConcurrentHashMap<>();
  }

  /**
   * 수정에 참여하는 사용자를 추가하는 Thread-safe 메소드입니다.
   *
   * @param user 수정에 참여한 사용자
   */
  public void addParticipant(User user) {
    participants.put(user.getId(), user);
  }

  /**
   * 동시 편집에 더 이상 참여하지 않는 사용자를 제거하는 Thread-safe 메소드입니다.
   *
   * @param user 수정자 목록에서 제거할 사용자
   */
  public void removeParticipant(User user) {
    participants.remove(user.getId());
  }

  /**
   * 수정에 참여하는 사용자 목록을 반환합니다. item 을 추가 혹은 삭제할 수 없도록 `unmodifiableMap` 으로 감싸서 반환합니다.
   *
   * @return 수정에 참여하는 사용자 목록
   */
  public Map<Long, User> getParticipants() {
    return Collections.unmodifiableMap(participants);
  }

  /**
   * 수정에 참여 중인 사용자 수를 반환합니다.
   * @return 수정에 참여 중인 사용자 수
   */
  public Integer getParticipantsCount() {
    return participants.size();
  }

  /**
   * 편집 사항을 적용하는 메소드입니다. LWWRegister 를 사용하여 편집 사항을 적용합니다.
   */
  public void applyEdit(LWWRegister<LWWNoteContent> others) {
    lwwRegister.merge(others);
  }
}
