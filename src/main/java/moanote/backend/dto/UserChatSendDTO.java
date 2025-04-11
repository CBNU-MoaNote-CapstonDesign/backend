package moanote.backend.dto;

/**
 * <pre>
 *   UserChatSendDTO
 *   유저 채팅 데이터 수신 DTO
 * </pre>
 * @param messageType 메시지의 성격 (유저 메시지, AI 기능 호출 등)
 * @param senderId 보낸 사람의 ID
 * @param messageContent 메시지 내용
 */
public record UserChatSendDTO(String messageType, String senderId, String messageContent) {

}
