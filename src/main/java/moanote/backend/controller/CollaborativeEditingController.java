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
   * <pre>
   * body : LWWStateDTO<LWWNoteContent>
   * {
   *  "stateId":String
   *  "timeStamp":Number,
   *  "value":{
   *      "content":String
   *    }
   *  }
   *  </pre>
   *
   * @param editedContent 문서 동기화를 위해 주고 받는 수정 사항
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
