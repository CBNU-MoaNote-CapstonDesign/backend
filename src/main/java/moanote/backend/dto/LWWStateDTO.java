package moanote.backend.dto;


/**
 * 문서 동기화를 위해 주고 받는 수정 사항 DTO
 *
 * @param stateId   LWW state ID
 * @param timeStamp LWW state timestamp
 * @param value     LWW state value
 * @param <T>       LWW state value type
 */
public record LWWStateDTO<T extends Record>(String stateId, int timeStamp, T value) {

  public LWWStateDTO {
    if (stateId == null) {
      throw new IllegalArgumentException("State ID cannot be null");
    }
  }
}
