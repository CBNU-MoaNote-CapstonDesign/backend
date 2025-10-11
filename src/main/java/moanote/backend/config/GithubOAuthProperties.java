package moanote.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * <pre>
 *   GithubOAuthProperties 는 GitHub OAuth 연동에 필요한 설정 값(clientId, secret 등)을 제공합니다.
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "github.oauth")
public class GithubOAuthProperties {

  @NotBlank
  private String clientId;

  @NotBlank
  private String clientSecret;

  @NotBlank
  private String redirectUri;

  private String scope = "repo";

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }
}
