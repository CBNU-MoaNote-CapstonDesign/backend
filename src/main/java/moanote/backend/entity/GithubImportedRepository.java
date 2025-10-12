package moanote.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * <pre>
 * GitHub 저장소를 가져올 때 생성된 루트 디렉터리와 연관된 메타데이터를 저장하는 엔터티입니다.
 * File 엔터티와 1:1 관계를 유지하여 디렉터리가 삭제될 경우 관련 정보도 함께 제거됩니다.
 * </pre>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "github_imported_repository")
public class GithubImportedRepository {

  @Id
  @Column(name = "file_id")
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "file_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private File rootDirectory;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private UserData user;

  @Column(name = "repository_name", nullable = false)
  private String repositoryName;

  @Column(name = "repository_url", nullable = false)
  private String repositoryUrl;

  public GithubImportedRepository(File rootDirectory, UserData user, String repositoryName,
      String repositoryUrl) {
    this.rootDirectory = rootDirectory;
    this.user = user;
    this.repositoryName = repositoryName;
    this.repositoryUrl = repositoryUrl;
  }
}