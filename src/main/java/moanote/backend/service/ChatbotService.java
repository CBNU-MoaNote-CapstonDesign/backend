package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.dto.UserChatSendDTO;
import moanote.backend.entity.UserData;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public ChatbotService(SimpMessagingTemplate messagingTemplate,
      @Value("{agent.api.url}") String AGENT_API_URL, @Value("{agent.name}") String AGENT_NAME,
      UserDataRepository userDataRepository, UserService userService) {
    this.messagingTemplate = messagingTemplate;
    this.userDataRepository = userDataRepository;
    this.AGENT_API_URL = AGENT_API_URL;
    this.userService = userService;

    this.AGENT_NAME = AGENT_NAME;
    UUID uuid;
    try {
      uuid = userDataRepository.findByUsername(AGENT_NAME).getId();
    } catch (Exception e) {
      UserData agent = userService.createUser(AGENT_NAME, "1234");;
      uuid = agent.getId();
    }
    this.AGENT_UUID = uuid;
  }

  /**
   * Chatbot 을 위한 UserChatMessageBroadcastDTO 팩토리 메소드
   *
   * @param content 메시지 내용
   * @return UserChatMessageBroadcastDTO
   */
  private UserChatMessageBroadcastDTO buildBroadcastMessage(String content) {
    String messageType = "bot";
    String date = LocalDateTime.now().atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String chatId = UuidCreator.getTimeOrderedEpoch().toString();

    return new UserChatMessageBroadcastDTO(messageType, AGENT_UUID.toString(), AGENT_NAME, date,
        content, chatId);
  }

  private void broadcastMessage(String channelId, UserChatMessageBroadcastDTO message) {
    System.out.println("다음에 보냅니다 : /topic/chat/channel/" + channelId);
    messagingTemplate.convertAndSend("/topic/chat/channel/" + channelId, message);
  }

  public void handleEditChat(String channelId, UserChatMessageBroadcastDTO message) {
    broadcastMessage(channelId, message);
  }

  /**
   * 유저의 AI 요청 메시지를 받아서, python agent 에게 요청을 보냄
   *
   * @param channelId 채팅 채널 ID
   * @param userRequest 유저의 요청 메시지
   */
  @Async
  public void handleUserBotRequest(String channelId, UserChatSendDTO userRequest) {
    if (!userRequest.messageType().equals("request-bot")) {
      return;
    }

    String content = userRequest.messageContent();

    // 1. python agent 호출 -> FastAPI 호출
    Map<String, String> body = Map.of("channelId", channelId, "content", content);

    webClient.post().uri(AGENT_API_URL + "/request").bodyValue(body).retrieve().toBodilessEntity()
        .subscribe();
  }

  /**
   * `ChatbotController::chat` 로 부터 호출됨
   *
   * @param channelId 채팅 채널 ID
   * @return UserChatMessageBroadcastDTO
   */
  public UserChatMessageBroadcastDTO handleBuildChat(String channelId) {
    UserChatMessageBroadcastDTO message = buildBroadcastMessage("...");
    broadcastMessage(channelId, message);
    return message;
  }
}
