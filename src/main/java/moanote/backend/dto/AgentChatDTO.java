package moanote.backend.dto;

/**
 * <pre>
 *   Agent가 AgentController API에 chat을 보낼 때 처리하는 DTO
 * </pre>
 * @param content 메시지의 성격 (유저 메시지, AI 기능 호출 등)
 */
public record AgentChatDTO(String content) {

}
