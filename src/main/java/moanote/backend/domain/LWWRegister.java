package moanote.backend.domain;

import lombok.AccessLevel;
import lombok.Getter;
import java.util.Optional;

/**
 * LWWRegister 은 Last-Write-Wins Register 의 약자로, 가장 최근에 업데이트된 값을 가지는 레지스터를 의미한다. State-based CRDT 중
 * 하나이다.
 *
 * @param <T> 동기화 할 데이터 타입. Record 이어야 한다.
 */
@Getter
public class LWWRegister<T extends Record> {

  /**
   * stateId 는 timeStamp 가 같을 때, 두 레지스터의 값을 비교할 때 사용된다. Tie-breaker 로 사용된다.
   */
  private String stateId;

  /**
   * 논리 시계로, 레지스터의 업데이트 시간을 나타낸다.
   */
  private int timeStamp;

  @Getter(AccessLevel.NONE)
  private T value;

  public LWWRegister(String stateId, int timeStamp, T value) {
    this.stateId = stateId;
    this.timeStamp = timeStamp;
    this.value = value;
  }

  public LWWRegister(String stateId) {
    this.stateId = stateId;
    this.timeStamp = 0;
    this.value = null;
  }

  /**
   * 다른 레지스터와 병합한다. Thread-Safe 하게 동작한다.
   *
   * @param other 병합할 레지스터
   */
  public boolean merge(LWWRegister<T> other) {
    String othersStateId = other.getStateId();
    int othersTimeStamp = other.getTimeStamp();
    T othersValue = other.getValue().orElse(null);

    synchronized (this) {
      if (othersTimeStamp > this.timeStamp || (othersTimeStamp == this.timeStamp
          && othersStateId.compareTo(this.stateId) > 0)) {
        this.stateId = othersStateId;
        this.timeStamp = othersTimeStamp;
        this.value = othersValue;
        return true;
      }
      return false;
    }
  }

  public Optional<T> getValue() {
    return Optional.ofNullable(value);
  }
}