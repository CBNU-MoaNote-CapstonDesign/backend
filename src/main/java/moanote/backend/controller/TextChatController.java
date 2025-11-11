package moanote.backend.controller;


import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.dto.UserChatSendDTO;
import moanote.backend.service.ChatbotService;
import moanote.backend.service.TextChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TextChatController {

  private final TextChatService textChatService;
  private final ChatbotService chatbotService;

  @Autowired
  public TextChatController(TextChatService textChatService, ChatbotService chatbotService) {
    this.textChatService = textChatService;
    this.chatbotService = chatbotService;
  }

  /**
   * UserChatSendDTO 를 받아 /topic 에 구독한 유저에게 메시지를 전파한다.
   * @param body UserChatSendDTO
   * @param channelId 채널 ID. 동일한 채널에 있는 유저에게만 메시지를 전파한다.
   * @return UserChatMessageBroadcastDTO
   */
  @MessageMapping("/chat/channel/{channelId}")
  @SendTo("/topic/chat/channel/{channelId}")
  public UserChatMessageBroadcastDTO sendChatMessage(UserChatSendDTO body,
      @DestinationVariable("channelId") String channelId) {

    chatbotService.handleUserBotRequest(channelId, body);

    System.out.println("Chatting in channel: " + channelId);
    return textChatService.receive(body);
  }
}
