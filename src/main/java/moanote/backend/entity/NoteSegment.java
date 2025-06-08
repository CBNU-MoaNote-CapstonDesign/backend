package moanote.backend.entity;

import java.util.UUID;

/**
 * Note 의 내용을 구성하는 세그먼트의 인터페이스입니다. 세그먼트의 내용에 관련없이 일반화하여 다룰 때 사용할 수 있습니다.
 */
public interface NoteSegment {

  UUID getId();

  Note getNote();
}
