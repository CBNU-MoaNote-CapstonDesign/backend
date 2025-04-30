package moanote.backend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.dto.UserChatSendDTO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


/**
 * <pre>
 *   Text 기반의 채팅 기능을 처리하는 서비스입니다.
 * </pre>
 */
@Service
public class TextChatService {

  /**
   * <pre>
   *   유저가 보낸 메시지를 받아서 처리하는 entry point 입니다.
   *   메시지 타입에 따라 적절한 메서드를 호출합니다.
   * </pre>
   * @param message 유저가 보낸 메시지 DTO
   * @return 필요한 데이터가 추가되어 가공된 메시지 DTO
   */
  public UserChatMessageBroadcastDTO receive(UserChatSendDTO message) {
    // if (message.messageType() == )
    return receiveUserToUserChatMessage(message);
  }

  /**
   * <pre>
   *   유저가 다른 유저를 대상으로 보낸 메시지를 받아서 가공하여 반환합니다.
   *   보낸 시각, senderName, chatId 데이터를 추가합니다.
   * </pre>
   * @param message 유저가 보낸 메시지 DTO
   * @return 필요한 데이터가 추가되어 가공된 메시지 DTO
   */
  private UserChatMessageBroadcastDTO receiveUserToUserChatMessage(UserChatSendDTO message) {
    String messageContent = message.messageContent();

    // TODO@ Authority 기능 구현 후 이를 통한 user id 확인 및 username 불러오기
    String senderId = message.senderId();
    String senderName = message.senderId();
    String date = LocalDateTime.now().atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String chatId = UuidCreator.getTimeOrderedEpoch().toString();
    return new UserChatMessageBroadcastDTO(message.messageType(), senderId, senderName, date, messageContent,
        chatId);
  }
}
