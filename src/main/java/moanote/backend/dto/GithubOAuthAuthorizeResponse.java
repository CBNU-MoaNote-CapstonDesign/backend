package moanote.backend.dto;

/**
 * <pre>
 *   GithubOAuthAuthorizeResponse 는 프론트엔드가 리다이렉트해야 할 GitHub Authorization URL 을 제공합니다.
 * </pre>
 *
 * @param authorizationUrl GitHub Authorization URL
 * @param state            CSRF 방지용 state 값
 */
public record GithubOAuthAuthorizeResponse(String authorizationUrl, String state) {
}
