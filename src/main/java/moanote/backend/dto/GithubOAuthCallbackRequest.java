package moanote.backend.dto;

import java.util.UUID;

/**
 * <pre>
 *   GithubOAuthCallbackRequest 는 GitHub OAuth 콜백으로부터 전달된 code 와 state 를 전달합니다.
 * </pre>
 *
 * @param userId OAuth 를 수행한 사용자 ID
 * @param code   GitHub Authorization Code
 * @param state  GitHub 이 반환한 state 값
 */
public record GithubOAuthCallbackRequest(UUID userId, String code, String state) {
}
