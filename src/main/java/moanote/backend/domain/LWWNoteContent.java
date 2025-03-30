package moanote.backend.domain;

/**
 * <pre>
 * LWWRegister 을 통해 동기화되는 Value 데이터입니다.
 * 해당 레코드의 동기화 대상은 Note 의 content 입니다.
 * </pre>
 * @param content
 */
public record LWWNoteContent(String content) {}
