package moanote.backend.dto;

import java.util.UUID;

/**
 * <pre>
 *   유저가 보낸 채팅 메시지를 가공하여 전파할 때 사용하는 DTO
 *
 *   TODO@ messageType 은 스펙이 정해지면 Enum 으로 변경 필요
 * </pre>
 * @param messageType 메시지의 성격 (유저 메시지, AI 기능 호출 등)
 * @param senderId 보낸 사람의 ID
 * @param senderName 보낸 사람의 이름
 * @param date 보낸 날짜 (UTC-0) 서버에서 메시지를 처리한 시간입니다.
 *             ex) 2023-10-01T12:00:00Z
 * @param messageContent 메시지 내용
 * @param chatId 채팅 객체 ID (UUIDv7)
 */
public record UserChatMessageBroadcastDTO(String messageType, UUID senderId, String senderName,
                                          String date, String messageContent, UUID chatId) {

}
