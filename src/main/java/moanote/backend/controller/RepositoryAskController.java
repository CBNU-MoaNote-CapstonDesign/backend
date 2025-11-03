package moanote.backend.controller;

import moanote.backend.dto.RepositoryAskDTO;
import moanote.backend.service.RepositoryAskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 유저가 불러온 저장소에 대해 질문을 할 수 있는 API 컨트롤러
 */
@RestController
@RequestMapping("/api/repository")
public class RepositoryAskController {

  private final RepositoryAskService repositoryAskService;

  @Autowired
  public RepositoryAskController(RepositoryAskService repositoryAskService) {
    this.repositoryAskService = repositoryAskService;
  }

  /**
   * 유저가 불러온 저장소에 대해 질문을 처리하는 엔드포인트
   *
   * @param request 질문 요청 쿼리
   * @return 질문에 대한 응답
   */
  @PostMapping("/ask")
  public ResponseEntity<?> ask(@RequestParam(name = "user") UUID userId, @RequestBody RepositoryAskDTO request) {
    try {
      return ResponseEntity.ok().body(repositoryAskService.ask(userId, request));
    } catch (NoSuchElementException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(404).body(null);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(403).body(null);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(500).body(null);
    }
  }
}
