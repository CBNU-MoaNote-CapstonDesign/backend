package moanote.backend.controller;

import com.github.f4b6a3.uuid.util.UuidValidator;
import moanote.backend.domain.LWWNoteContent;
import moanote.backend.dto.CRDTOperationDTO;
import moanote.backend.dto.CaretDTO;
import moanote.backend.dto.LWWStateDTO;
import moanote.backend.dto.TextEditParticipateDTO;
import moanote.backend.service.LWWCollaborativeEditingService;
import moanote.backend.service.TextCollaborativeEditingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.UUID;

@Controller
public class CollaborativeEditingController {

  private final LWWCollaborativeEditingService diagramCollaborativeEditingService;

  private final TextCollaborativeEditingService textCollaborativeEditingService;

  @Autowired
  public CollaborativeEditingController(
      LWWCollaborativeEditingService diagramCollaborativeEditingService,
      TextCollaborativeEditingService textCollaborativeEditingService) {
    this.diagramCollaborativeEditingService = diagramCollaborativeEditingService;
    this.textCollaborativeEditingService = textCollaborativeEditingService;
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
  @MessageMapping("/docs/diagram/edit/{segmentId}")
  @SendTo("/topic/docs/{segmentId}")
  public LWWStateDTO<LWWNoteContent> editingDocs(LWWStateDTO<LWWNoteContent> editedContent,
      @DestinationVariable("segmentId") String segmentId) {
    System.out.println("Edited content received");
    // TODO@ (ACLService) ACL check here

    if (!UuidValidator.isValid(segmentId)) {
      System.out.println("Doc ID not valid");
      return null;
    }
    diagramCollaborativeEditingService.editSegment(editedContent, UUID.fromString(segmentId));
    return editedContent;
  }

  /**
   * 동시 편집을 시작하려는 사용자가 구독을 요청할 때 호출되는 메서드. STOMP Message header 에 "participantUserId" 속성이 있어야 함
   *
   * @param messageHeaderAccessor STOMP message header accessor "participantUserId" 속성을 요구함
   * @param segmentId             세그먼트 ID
   * @return LWWStateDTO<LWWNoteContent> 세션의 현재 LWWState 를 담고 있는 객체 DTO
   */
  @SubscribeMapping("/docs/diagram/participate/{segmentId}")
  public LWWStateDTO<LWWNoteContent> participateSession(
      SimpMessageHeaderAccessor messageHeaderAccessor, @DestinationVariable("segmentId") String segmentId) {
    String participantUserId = messageHeaderAccessor.getFirstNativeHeader("participantUserId");
    System.out.println("User Access : " + participantUserId);
    if (!UuidValidator.isValid(participantUserId)) {
      System.out.println("User Access not valid");
      return null;
    }
    if (!UuidValidator.isValid(segmentId)) {
      System.out.println("Doc ID not valid");
      return null;
    }
    return diagramCollaborativeEditingService.participateSession(
        UUID.fromString(participantUserId),
        UUID.fromString(segmentId));
  }

  @MessageMapping("/docs/text/edit/{noteId}/{segmentId}")
  @SendTo("/topic/docs/text/{noteId}/{segmentId}")
  public List<CRDTOperationDTO> editingDocs(List<CRDTOperationDTO> editOperations,
      @DestinationVariable("segmentId") UUID segmentId,
      @DestinationVariable("noteId") UUID noteId) {
    System.out.println("Edit operation received: " + editOperations);
    textCollaborativeEditingService.editSegment(editOperations, segmentId, noteId);
    return editOperations;
  }

  /**
   * 동시 편집을 시작하려는 사용자가 구독을 요청할 때 호출되는 메서드. STOMP Message header 에 "participantUserId" 속성이 있어야 함
   *
   * @param messageHeaderAccessor STOMP message header accessor "participantUserId" 속성을 요구함
   * @param noteId                 노트 ID
   * @return LWWStateDTO<LWWNoteContent> 세션의 현재 LWWState 를 담고 있는 객체 DTO
   */
  @SubscribeMapping("/docs/text/participate/{noteId}")
  public TextEditParticipateDTO participateTextEditSession(
      SimpMessageHeaderAccessor messageHeaderAccessor, @DestinationVariable("noteId") String noteId) {
    String participantUserId = messageHeaderAccessor.getFirstNativeHeader("participantUserId");
    System.out.println("User Access : " + participantUserId);
    if (!UuidValidator.isValid(participantUserId)) {
      System.out.println("User Access not valid");
      return null;
    }
    if (!UuidValidator.isValid(noteId)) {
      System.out.println("Doc ID not valid");
      return null;
    }
    return textCollaborativeEditingService.participateSession(UUID.fromString(participantUserId),
        UUID.fromString(noteId));
  }

  @MessageMapping("/docs/text/caret/{noteId}/{segmentId}")
  @SendTo("/topic/docs/caret/{noteId}/{segmentId}")
  public Object editingDocs(CaretDTO caretDTO,
      @DestinationVariable("segmentId") UUID segmentId,
      @DestinationVariable("noteId") UUID noteId) {
    return caretDTO;
  }
}