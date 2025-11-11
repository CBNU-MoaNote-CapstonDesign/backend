package moanote.backend.domain;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class LWWRegisterTest {

  /**
   * list의 다음 순열을 구한다.
   * @param list 다음 순열을 구할 리스트
   * @return 다음 순열이 있으면 true, 없으면 false
   * @param <T> 리스트의 타입
   */
  <T extends Comparable<T>>
  boolean nextPermutation(ArrayList<T> list) {
    int i = list.size() - 1;
    while (i > 0 && list.get(i - 1).compareTo(list.get(i)) >= 0) {
      i--;
    }
    if (i <= 0) {
      return false;
    }
    int j = list.size() - 1;
    while (list.get(j).compareTo(list.get(i - 1)) <= 0) {
      j--;
    }
    T temp = list.get(i - 1);
    list.set(i - 1, list.get(j));
    list.set(j, temp);
    j = list.size() - 1;
    while (i < j) {
      temp = list.get(i);
      list.set(i, list.get(j));
      list.set(j, temp);
      i++;
      j--;
    }
    return true;
  }
  record TextValue(String content) {}

  /**
   * editCount 번의 수정이 3명의 사용자로부터 발생하는 경우를 테스트한다.
   * 각 사용자가 수정한 순서대로 수정을 적용하고, 최종적으로 합치는 과정에서
   * 합치는 순서가 달라도 최종적으로는 같은 값이 나오는지 확인한다.
   */
  @Test
  void testEventualConsistency() {
    int editCount = 7;
    String[] userid = {"user0", "user1", "user2"};
    LinkedList<LWWRegister<TextValue>> lwwRegisterList = new LinkedList<>();
    ArrayList<Integer> editOrder = new ArrayList<>(editCount);
    ArrayList<Integer> editByWho = new ArrayList<>(editCount);

    for (int i = 0; i < 10; i++) {
      editByWho.add(0);
    }

    while (true) {
      lwwRegisterList = new LinkedList<>();
      LWWRegister<TextValue> finalLwwRegister = null;

      int[] editCountByWho = {0, 0, 0};
      Integer cnt = 0;
      for (Integer editor : editByWho) {
        cnt++;
        editCountByWho[editor]++;
        lwwRegisterList.add(new LWWRegister<>(userid[editor],
            editCountByWho[editor], new TextValue(Integer.toString(cnt))));
      }

      editOrder = new ArrayList<>(editCount);
      for (int i = 0; i < editCount; i++) {
        editOrder.add(i);
      }
      do {
        LWWRegister<TextValue> lwwRegisterNow = new LWWRegister<>(userid[0]);
        for (int i = 0; i < editCount; i++) {
          lwwRegisterNow.merge(lwwRegisterList.get(editOrder.get(i)));
        }
        if (finalLwwRegister == null) {
          finalLwwRegister = lwwRegisterNow;
          continue;
        }
        assertEquals(finalLwwRegister.getValue().get(), lwwRegisterNow.getValue().get());
      } while (nextPermutation(editOrder));

      boolean isEnd = true;
      for (int i = editCount - 1; i >= 0; i--) {
        if (editByWho.get(i) < 2) {
          editByWho.set(i, editByWho.get(i) + 1);
          isEnd = false;
          break;
        } else {
          editByWho.set(i, 0);
        }
      }
      if (isEnd)
        break;
    }
  }
}