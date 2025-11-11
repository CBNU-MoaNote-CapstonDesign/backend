package moanote.backend.dto;

import java.util.UUID;

/**
 * <pre>
 *   GithubOAuthAuthorizeRequest 는 OAuth 인증 절차를 시작하기 위해 필요한 사용자 식별자를 전달합니다.
 * </pre>
 *
 * @param userId OAuth 를 시작할 사용자 ID
 */
public record GithubOAuthAuthorizeRequest(UUID userId) {
}
