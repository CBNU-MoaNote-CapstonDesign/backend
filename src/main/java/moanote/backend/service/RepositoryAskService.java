package moanote.backend.service;

import moanote.backend.dto.RepositoryAskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RepositoryAskService {

  final private GithubIntegrationService githubIntegrationService;

  final private RestTemplate restTemplate = new RestTemplate();

  final private String externalGraphBaseUrl;

  @Autowired
  public RepositoryAskService(GithubIntegrationService githubIntegrationService,
      @Value("${codebaseExplorer.api.url}") String externalGraphBaseUrl) {
    this.githubIntegrationService = githubIntegrationService;
    this.externalGraphBaseUrl = externalGraphBaseUrl;
  }

  public Map<String, Object> ask(UUID userId, RepositoryAskDTO repositoryAskDTO) {
    if (repositoryAskDTO.question() == null) throw new IllegalArgumentException("question must not be null");
    if (repositoryAskDTO.repositoryUrl() == null) throw new IllegalArgumentException("repository must not be null");
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(askDTOTransform(userId, repositoryAskDTO.question(), repositoryAskDTO.repositoryUrl()), headers);

    ResponseEntity<Map> response =
        restTemplate.postForEntity(externalGraphBaseUrl + "/graph/ask", requestEntity, Map.class);

    return response.getBody();
  }
  
  public Map<String, String> askDTOTransform(UUID userId, String question, String repositoryUrl) {
    Map<String, String> dto = new HashMap<>();

    Path repoPath = githubIntegrationService.resolveExistingRepositoryPath(userId, repositoryUrl);
    if (repoPath == null) throw new IllegalStateException("Failed to resolve repository path for: " + repositoryUrl);

    dto.put("question", question);
    dto.put("projectPath", repoPath.toAbsolutePath().toString());

    return dto;
  }
}