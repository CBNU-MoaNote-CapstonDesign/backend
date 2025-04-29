package moanote.backend.controller;

import com.github.f4b6a3.uuid.util.UuidValidator;
import moanote.backend.domain.LWWNoteContent;
import moanote.backend.dto.LWWStateDTO;
import moanote.backend.service.CollaborativeEditingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import java.util.UUID;

@Controller
public class CollaborativeEditingController {

  private final CollaborativeEditingService collaborativeEditingService;

  @Autowired
  public CollaborativeEditingController(CollaborativeEditingService collaborativeEditingService) {
    this.collaborativeEditingService = collaborativeEditingService;
  }

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
  @SendTo("/app/docs/{docId}")
  public LWWStateDTO<LWWNoteContent> editingDocs(LWWStateDTO<LWWNoteContent> editedContent,
      @DestinationVariable("docId") String docId) {
    System.out.println("Edited content received");
    // TODO@ (ACLService) ACL check here

    if (!UuidValidator.isValid(docId)) {
      System.out.println("Doc ID not valid");
      return null;
    }
    collaborativeEditingService.editNote(editedContent, UUID.fromString(docId));
    return editedContent;
  }

  /**
   * 동시 편집을 시작하려는 사용자가 구독을 요청할 때 호출되는 메서드. STOMP Message header 에 "participantUserId" 속성이 있어야 함
   *
   * @param messageHeaderAccessor STOMP message header accessor "participantUserId" 속성을 요구함
   * @param docId                 문서 ID
   * @return LWWStateDTO<LWWNoteContent> 세션의 현재 LWWState 를 담고 있는 객체 DTO
   */
  @SubscribeMapping("/docs/{docId}")
  public LWWStateDTO<LWWNoteContent> participateSession(
      SimpMessageHeaderAccessor messageHeaderAccessor, @DestinationVariable("docId") String docId) {
    String participantUserId = messageHeaderAccessor.getFirstNativeHeader("participantUserId");
    System.out.println("User Access : " + participantUserId);
    if (!UuidValidator.isValid(participantUserId)) {
      System.out.println("User Access not valid");
      return null;
    }
    if (!UuidValidator.isValid(docId)) {
      System.out.println("Doc ID not valid");
      return null;
    }
    return collaborativeEditingService.participateSession(
        UUID.fromString(participantUserId),
        UUID.fromString(docId));
  }
}
