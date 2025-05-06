package moanote.backend.controller;

import moanote.backend.domain.LWWNoteContent;
import moanote.backend.dto.LWWStateDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class CollaborativeEditingController {

  /**
   * 문서 수정 사항을 수신 및 전파하는 메소드
   *
   * @param editedContent 문서 동기화를 위해 주고 받는 수정 사항
   * @param docId         문서 ID
   * @return editedContent 와 동일한 Scheme 의 객체로, editedContent 와 서버의 LWWState 를 Merge 한 상태
   */
  @MessageMapping("/docs/edit/{docId}")
  @SendTo("/topic/docs/{docId}")
  public LWWStateDTO<LWWNoteContent> editingDocs(LWWStateDTO<LWWNoteContent> editedContent,
      @DestinationVariable("docId") String docId) {
    System.out.println("Edited content received");
    // TODO@ (ACLService) ACL check here
    // TODO@ (CollaborativeService) 서버의 문서를 동기화하는 로직을 추가해야 함
    return editedContent;
  }

}
