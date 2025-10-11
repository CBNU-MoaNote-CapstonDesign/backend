package moanote.backend.controller;

import moanote.backend.dto.FileDTO;
import moanote.backend.dto.GithubBranchCommitRequest;
import moanote.backend.dto.GithubFetchRequest;
import moanote.backend.dto.GithubImportRequest;
import moanote.backend.service.GithubIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * <pre>
 *   GithubController 는 GitHub 연동과 관련된 REST 엔드포인트를 제공합니다.
 * </pre>
 */
@RestController
@RequestMapping("/api/github")
public class GithubController {

  private final GithubIntegrationService githubIntegrationService;

  public GithubController(GithubIntegrationService githubIntegrationService) {
    this.githubIntegrationService = githubIntegrationService;
  }

  /**
   * <pre>
   *   GitHub 저장소를 불러와 텍스트 기반 파일을 애플리케이션에 추가합니다.
   * </pre>
   *
   * @param request 저장소 URL, 사용자 정보 및 인증 정보를 포함한 요청 본문
   * @return 생성된 파일 목록
   */
  @PostMapping("/import")
  public ResponseEntity<List<FileDTO>> importRepository(@RequestBody GithubImportRequest request) {
    try {
      List<FileDTO> imported = githubIntegrationService.importRepository(request.userId(), request.repositoryUrl(),
          request.credentials());
      return ResponseEntity.ok(imported);
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(404).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * <pre>
   *   로컬 저장소에서 새 브랜치를 생성하고 커밋 후 원격 저장소로 푸시합니다.
   * </pre>
   *
   * @param request 브랜치 이름, 커밋 메시지, 파일 내용 및 인증 정보를 포함한 요청 본문
   * @return 성공 시 204 No Content
   */
  @PostMapping("/branch")
  public ResponseEntity<Void> createBranchAndCommit(@RequestBody GithubBranchCommitRequest request) {
    try {
      githubIntegrationService.createBranchAndCommit(request.userId(), request.repositoryUrl(), request.baseBranch(),
          request.branchName(), request.commitMessage(), request.files(), request.credentials());
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * <pre>
   *   로컬 저장소에서 지정된 브랜치를 원격과 동기화하며, 충돌을 방지하기 위해 로컬 변경 사항을 삭제합니다.
   * </pre>
   *
   * @param request 사용자, 저장소 URL, 브랜치 및 인증 정보를 포함하는 요청 본문
   * @return 성공 시 204 No Content
   */
  @PostMapping("/fetch")
  public ResponseEntity<Void> fetchRepository(@RequestBody GithubFetchRequest request) {
    try {
      githubIntegrationService.fetchRepository(request.userId(), request.repositoryUrl(), request.branchName(),
          request.credentials());
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}
