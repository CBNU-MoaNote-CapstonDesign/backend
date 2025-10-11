package moanote.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

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

  @DurationUnit(ChronoUnit.MINUTES)
  private Duration stateTtl = Duration.ofMinutes(10);

  @DurationUnit(ChronoUnit.HOURS)
  private Duration tokenTtl = Duration.ofHours(1);

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

  public Duration getStateTtl() {
    return stateTtl;
  }

  public void setStateTtl(Duration stateTtl) {
    this.stateTtl = stateTtl;
  }

  public Duration getTokenTtl() {
    return tokenTtl;
  }

  public void setTokenTtl(Duration tokenTtl) {
    this.tokenTtl = tokenTtl;
  }
}
