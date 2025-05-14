package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.dto.UserChatSendDTO;
import moanote.backend.entity.UserData;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Chatbot Agent와 채팅 간 연결하는 Service
 */
@Service
public class ChatbotService {

  private final WebClient webClient = WebClient.create();

  private final SimpMessagingTemplate messagingTemplate;

  private final UUID AGENT_UUID;
  private final String AGENT_NAME;

  private final String AGENT_API_URL;
  private final UserDataRepository userDataRepository;
  private final UserService userService;

  public ChatbotService(SimpMessagingTemplate messagingTemplate,
      @Value("{agent.api.url}") String AGENT_API_URL,
      @Value("{agent.name}") String AGENT_NAME, UserDataRepository userDataRepository,
      UserService userService) {
    this.messagingTemplate = messagingTemplate;
    this.userDataRepository = userDataRepository;
    this.AGENT_API_URL = AGENT_API_URL;
    this.userService = userService;

    this.AGENT_NAME = AGENT_NAME;
    UUID uuid;
    try {
      uuid = userDataRepository.findByUsername(AGENT_NAME).getId();
    } catch (Exception e) {
      UserData agent = buildAgent(AGENT_NAME);
      uuid = agent.getId();
    }
    this.AGENT_UUID = uuid;
  }

  private UserData buildAgent(String name) {
    return userService.createUser(name, "1234");
  }

  private UserChatMessageBroadcastDTO buildBroadcastMessage(String content) {
    String senderName = AGENT_NAME;
    UUID senderId = AGENT_UUID;

    String messageType = "bot";
    String date = LocalDateTime.now().atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String chatId = UuidCreator.getTimeOrderedEpoch().toString();

    return new UserChatMessageBroadcastDTO(messageType, senderId.toString(), senderName, date, content,
        chatId);
  }

  private void broadcastMessage(String channelId, UserChatMessageBroadcastDTO message) {
    System.out.println("다음에 보냅니다 : /topic/chat/channel/" + channelId);
    messagingTemplate.convertAndSend("/topic/chat/channel/" + channelId, message);
  }

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
        "channelId", channelId,
        "content", content
    );

    webClient.post()
        .uri(AGENT_API_URL + "/request")
        .bodyValue(body)
        .retrieve()
        .toBodilessEntity()
        .subscribe();
  }

  public UserChatMessageBroadcastDTO handleBuildChat(String channelId) {
    UserChatMessageBroadcastDTO message = buildBroadcastMessage("...");
    broadcastMessage(channelId, message);
    return message;
  }
}
