package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.dto.UserChatSendDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Chatbot Agent와 채팅 간 연결하는 Service
 */
@Service
public class ChatbotService {
    @Value("${agent.uuid}")
    private String AGENT_UUID;

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

    @Async
    public void handleBotRequest(String channelId, UserChatSendDTO userRequest) {
        if (!userRequest.messageType().equals("request-bot")) {
            return;
        }

        String content = userRequest.messageContent();

        // 1. python agent 호출 -> FastAPI 호출
        // response : chating창에다 생성중 상태 표시해줘
        //
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String[] messages = {"안녕하세요, Moa Bot 입니다. 무엇을 도와드릴까요?","제게 요청을 주신 분의 성함은 'kim' 님 이십니다."};
        UserChatMessageBroadcastDTO botResponse = buildBroadcastMessage(messages[mid++]);
        broadcastMessage(channelId, botResponse);
    }
}
