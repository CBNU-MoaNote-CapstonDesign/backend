package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.dto.UserChatSendDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Chatbot Agent와 채팅 간 연결하는 Service
 */
@Service
public class ChatbotService {
  @Value("${agent.api.url}")
  private String AGENT_API_URL ;

  @Value("${agent.uuid}")
  private String AGENT_UUID;

  private final WebClient webClient = WebClient.create();

  private final SimpMessagingTemplate messagingTemplate;

  public ChatbotService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  private UserChatMessageBroadcastDTO buildBroadcastMessage(String content) {
    String senderId = AGENT_UUID;
    String senderName = "Moa Bot";
    String messageType = "bot";
    String date = LocalDateTime.now().atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String chatId = UuidCreator.getTimeOrderedEpoch().toString();

    return new UserChatMessageBroadcastDTO(messageType, senderId, senderName, date, content,
        chatId);
  }

  private void broadcastMessage(String channelId, UserChatMessageBroadcastDTO message) {
    System.out.println("다음에 보냅니다 : /topic/chat/channel/" + channelId);
    messagingTemplate.convertAndSend("/topic/chat/channel/" + channelId, message);
  }

  private int mid = 0;

  public void handleEditChat(String channelId, UserChatMessageBroadcastDTO message) {
    broadcastMessage(channelId, message);
  }

  @Async
  public void handleBotRequest(String channelId, UserChatSendDTO userRequest) {
    if (!userRequest.messageType().equals("request-bot")) {
      return;
    }

    String content = userRequest.messageContent();

    // 1. python agent 호출 -> FastAPI 호출
    Map<String, String> body = Map.of(
        "channelId", channelId
    );

    webClient.post()
        .uri(AGENT_API_URL+"/request")
        .bodyValue(body)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  public UserChatMessageBroadcastDTO handleBuildChat(String channelId) {
    UserChatMessageBroadcastDTO message = buildBroadcastMessage("...");
    broadcastMessage(channelId, message);
    return message;
  }
}
