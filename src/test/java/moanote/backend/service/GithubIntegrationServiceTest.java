package moanote.backend.service;

import moanote.backend.BackendApplication;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.GithubImportedRepositoryDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.Note.CodeLanguage;
import moanote.backend.entity.Note.NoteType;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.GithubImportedRepositoryRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class GithubIntegrationServiceTest {

  @Autowired
  private GithubIntegrationService githubIntegrationService;

  @Autowired
  private UserService userService;

  @Autowired
  private FileService fileService;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private TextNoteSegmentRepository textNoteSegmentRepository;

  @Autowired
  private GithubImportedRepositoryRepository githubImportedRepositoryRepository;

  private static final String REPOSITORY_NAME = "sample-repo";

  private Path workspace;
  private Path remoteRepository;
  private final List<Path> cleanupTargets = new ArrayList<>();

  @BeforeEach
  void setUp() throws Exception {
    workspace = Files.createTempDirectory("github-test-");
    remoteRepository = workspace.resolve(REPOSITORY_NAME + ".git");
    Files.createDirectory(remoteRepository);
    try (Git ignored = Git.init()
        .setBare(true)
        .setInitialBranch("master")
        .setDirectory(remoteRepository.toFile())
        .call()) {
      // bare repository initialized
    }

    /*
     * Build a sample remote repository by cloning the bare repository into a working tree,
     * creating seed files, and pushing the resulting commit back to the bare origin.
     * This mirrors how a real remote repository would be prepared before importing.
     */
    Path seed = workspace.resolve("seed");
    Files.createDirectory(seed);
    try (Git git = Git.init()
        .setInitialBranch("master")
        .setDirectory(seed.toFile())
        .call()) {
      git.remoteAdd().setName("origin").setUri(new URIish(remoteRepository.toUri().toString())).call();
      Files.writeString(seed.resolve("README.md"), "Hello Repo\n", StandardCharsets.UTF_8);
      Files.createDirectories(seed.resolve("src"));
      Files.writeString(seed.resolve("src/Main.java"), "class Main {}\n", StandardCharsets.UTF_8);
      Files.write(seed.resolve("binary.bin"), new byte[]{0x1, 0x2, 0x3});
      git.add().addFilepattern(".").call();
      git.commit().setMessage("Initial commit").call();
      git.push().setRemote("origin").setRefSpecs(new RefSpec("refs/heads/master:refs/heads/master")).call();
    }
  }

  @AfterEach
  void tearDown() throws IOException {
    cleanupTargets.add(workspace);
    Path serviceWorkspace = workspaceRoot();
    if (serviceWorkspace != null) {
      cleanupTargets.add(serviceWorkspace);
    }
    for (Path path : cleanupTargets) {
      if (path != null && Files.exists(path)) {
        try (var paths = Files.walk(path)) {
          paths.sorted(Comparator.reverseOrder()).forEach(current -> {
            try {
              Files.deleteIfExists(current);
            } catch (IOException ignored) {
              // ignore cleanup issues
            }
          });
        }
      }
    }
    cleanupTargets.clear();
  }

  @Test
  @Transactional
  void importRepositoryCreatesDocumentsAndSkipsBinary() {
    UserData user = userService.createUser("github-user", "password");

    List<FileDTO> imported = githubIntegrationService.importRepository(user.getId(),
        remoteRepository.toUri().toString());

    scheduleWorkspaceCleanup(user.getId());

    assertThat(imported).hasSize(2);
    assertThat(imported).allMatch(FileDTO::githubImported);
    File root = fileRepository.getRootDirectory(user);
    UUID repositoryId = imported.stream()
        .filter(dto -> dto.name().equals("README.md"))
        .map(FileDTO::dir)
        .findFirst()
        .orElseThrow();
    File repoDirectory = fileRepository.findFileById(repositoryId).orElseThrow();
    assertThat(repoDirectory.getDirectory()).isEqualTo(root);
    assertThat(repoDirectory.getName()).isEqualTo("sample-repo");
    assertThat(repoDirectory.isGithubImported()).isTrue();

    List<File> rootChildren = fileRepository.findFilesByDirectory(repoDirectory);
    assertThat(rootChildren).extracting(File::getName)
        .containsExactlyInAnyOrder("README.md", "src");
    assertThat(rootChildren).extracting(File::getName).doesNotContain("binary.bin");
    assertThat(rootChildren).allMatch(File::isGithubImported);

    File srcDirectory = rootChildren.stream().filter(file -> file.getName().equals("src")).findFirst().orElseThrow();
    List<File> srcFiles = fileRepository.findFilesByDirectory(srcDirectory);
    File mainFile = srcFiles.stream().filter(file -> file.getName().equals("Main.java")).findFirst().orElseThrow();
    assertThat(mainFile.getType()).isEqualTo(FileType.DOCUMENT);
    assertThat(mainFile.getNote().getType()).isEqualTo(NoteType.CODE);
    assertThat(mainFile.getNote().getCodeLanguage()).isEqualTo(CodeLanguage.JAVA);
    assertThat(mainFile.isGithubImported()).isTrue();

    File readme = rootChildren.stream().filter(file -> file.getName().equals("README.md")).findFirst().orElseThrow();
    assertThat(readme.getNote().getType()).isEqualTo(NoteType.NORMAL);
    assertThat(readme.getNote().getCodeLanguage()).isEqualTo(CodeLanguage.TEXT);
    assertThat(readme.isGithubImported()).isTrue();

    TextNoteSegment segment = textNoteSegmentRepository.findAllByNote(mainFile.getNote()).getFirst();
    assertThat(readSegmentContent(segment)).isEqualTo("class Main {}\n");
  }

  @Test
  void listImportedRepositoriesReturnsMetadata() {
    UserData user = userService.createUser("list-user", "password");
    String repositoryUrl = remoteRepository.toUri().toString();

    githubIntegrationService.importRepository(user.getId(), repositoryUrl);
    scheduleWorkspaceCleanup(user.getId());

    List<GithubImportedRepositoryDTO> repositories =
        githubIntegrationService.listImportedRepositories(user.getId());

    assertThat(repositories)
        .containsExactly(new GithubImportedRepositoryDTO(REPOSITORY_NAME, repositoryUrl));
  }

  @Test
  void importingExistingRepositoryFails() {
    UserData user = userService.createUser("duplicate-user", "password");
    String repositoryUrl = remoteRepository.toUri().toString();

    githubIntegrationService.importRepository(user.getId(), repositoryUrl);
    scheduleWorkspaceCleanup(user.getId());

    assertThatThrownBy(() -> githubIntegrationService.importRepository(user.getId(), repositoryUrl))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already imported");

    assertThat(githubImportedRepositoryRepository.findByUser_Id(user.getId())).hasSize(1);
    File rootDirectory = fileRepository.getRootDirectory(user);
    long repositoryCount = fileRepository.findFilesByDirectory(rootDirectory).stream()
        .filter(file -> file.getName().equals(REPOSITORY_NAME))
        .count();
    assertThat(repositoryCount).isEqualTo(1);
  }

  @Test
  void deletingImportedRepositoryRemovesMetadata() {
    UserData user = userService.createUser("cleanup-user", "password");
    String repositoryUrl = remoteRepository.toUri().toString();

    githubIntegrationService.importRepository(user.getId(), repositoryUrl);
    scheduleWorkspaceCleanup(user.getId());

    assertThat(githubImportedRepositoryRepository.findByUser_Id(user.getId())).hasSize(1);

    File repositoryDirectory = findImportedRepositoryDirectory(user);
    fileService.deleteFile(repositoryDirectory.getId(), user.getId());
    githubImportedRepositoryRepository.flush();

    assertThat(githubImportedRepositoryRepository.findByUser_Id(user.getId())).isEmpty();
  }

  @Test
  @Transactional
  void createBranchAndCommitPushesToRemote() {
    UserData user = userService.createUser("branch-user", "password");
    githubIntegrationService.importRepository(user.getId(), remoteRepository.toUri().toString());
    scheduleWorkspaceCleanup(user.getId());

    githubIntegrationService.createBranchAndCommit(user.getId(), remoteRepository.toUri().toString(), "master",
        "feature/test", "Add feature", Map.of(REPOSITORY_NAME + "/FEATURE.txt", "new feature"));

    try (Git remote = Git.open(remoteRepository.toFile())) {
      assertThat(remote.getRepository().findRef("refs/heads/feature/test")).isNotNull();
      ObjectId branchHead = remote.getRepository().resolve("refs/heads/feature/test");
      Iterable<RevCommit> commits = remote.log().add(branchHead).call();
      boolean containsFeatureCommit = StreamSupport.stream(commits.spliterator(), false)
          .anyMatch(commit -> commit.getFullMessage().equals("Add feature"));
      assertThat(containsFeatureCommit).isTrue();
    } catch (IOException | GitAPIException e) {
      throw new IllegalStateException(e);
    }

    try (Git local = Git.open(resolveLocalRepositoryPath(user.getId()).toFile())) {
      assertThat(local.getRepository().getFullBranch()).isEqualTo("refs/heads/feature/test");
      assertThat(Files.exists(resolveLocalRepositoryPath(user.getId()).resolve("FEATURE.txt"))).isTrue();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  @Transactional
  void populateNoteContentCreatesSegmentAndNodes() {
    UserData user = userService.createUser("populate-user", "password");
    File document = fileService.createFile(user.getId(), "Doc.txt", FileType.DOCUMENT);

    ReflectionTestUtils.invokeMethod(githubIntegrationService, "populateNoteContent", document, "ABC");

    List<TextNoteSegment> segments = textNoteSegmentRepository.findAllByNote(document.getNote());
    assertThat(segments).hasSize(1);
    TextNoteSegment segment = segments.getFirst();

    assertThat(readSegmentContent(segment)).isEqualTo("ABC");
  }

  @Test
  @Transactional
  void fetchRepositoryUpdatesRemoteTrackingBranch() throws Exception {
    UserData user = userService.createUser("fetch-user", "password");
    githubIntegrationService.importRepository(user.getId(), remoteRepository.toUri().toString());
    Path localRepository = resolveLocalRepositoryPath(user.getId());
    scheduleWorkspaceCleanup(user.getId());

    File rootDirectory = fileRepository.getRootDirectory(user);
    File repositoryDirectory = fileRepository.findFilesByDirectory(rootDirectory).stream()
        .filter(file -> file.getName().equals(REPOSITORY_NAME))
        .findFirst()
        .orElseThrow();
    File readmeFile = fileRepository.findFilesByDirectory(repositoryDirectory).stream()
        .filter(file -> file.getName().equals("README.md"))
        .findFirst()
        .orElseThrow();
    TextNoteSegment originalReadmeSegment = textNoteSegmentRepository.findAllByNote(readmeFile.getNote()).getFirst();
    UUID originalSegmentId = originalReadmeSegment.getId();
    String originalReadmeContent = readSegmentContent(originalReadmeSegment);

    ObjectId before;
    try (Git local = Git.open(localRepository.toFile())) {
      before = local.getRepository().findRef("refs/remotes/origin/master").getObjectId();
    }

    createRemoteCommit("docs/CHANGELOG.md", "entry", "Add changelog");
    createRemoteCommit("README.md", "Updated README\n", "Rewrite README");

    githubIntegrationService.fetchRepository(user.getId(), remoteRepository.toUri().toString(), "master");

    try (Git local = Git.open(localRepository.toFile())) {
      ObjectId after = local.getRepository().findRef("refs/remotes/origin/master").getObjectId();
      assertThat(after).isNotNull();
      assertThat(after).isNotEqualTo(before);
    }

    assertThat(Files.readString(localRepository.resolve("docs/CHANGELOG.md"), StandardCharsets.UTF_8))
        .isEqualTo("entry");

    File refreshedRepositoryDirectory = fileRepository.findFileById(repositoryDirectory.getId()).orElseThrow();
    File refreshedReadme = fileRepository.findFileById(readmeFile.getId()).orElseThrow();
    List<TextNoteSegment> readmeSegments = textNoteSegmentRepository.findAllByNote(refreshedReadme.getNote());
    assertThat(readmeSegments).hasSize(1);
    TextNoteSegment updatedReadmeSegment = readmeSegments.getFirst();
    assertThat(updatedReadmeSegment.getId()).isNotEqualTo(originalSegmentId);
    assertThat(textNoteSegmentRepository.findById(originalSegmentId)).isEmpty();
    assertThat(readSegmentContent(updatedReadmeSegment)).isEqualTo("Updated README\n");
    assertThat(originalReadmeContent).isEqualTo("Hello Repo\n");

    File docsDirectory = fileRepository.findFilesByDirectory(refreshedRepositoryDirectory).stream()
        .filter(file -> file.getName().equals("docs"))
        .findFirst()
        .orElseThrow();
    File changelog = fileRepository.findFilesByDirectory(docsDirectory).stream()
        .filter(file -> file.getName().equals("CHANGELOG.md"))
        .findFirst()
        .orElseThrow();
    List<TextNoteSegment> changelogSegments = textNoteSegmentRepository.findAllByNote(changelog.getNote());
    assertThat(changelogSegments).hasSize(1);
    assertThat(readSegmentContent(changelogSegments.getFirst())).isEqualTo("entry");
  }

  private void createRemoteCommit(String filePath, String content, String message) throws GitAPIException, IOException {
    Path updater = workspace.resolve("updater");
    if (Files.exists(updater)) {
      try (var paths = Files.walk(updater)) {
        paths.sorted(Comparator.reverseOrder()).forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException ignored) {
            // ignore cleanup issues between commits
          }
        });
      }
    }
    Files.createDirectory(updater);
    try (Git git = Git.cloneRepository()
        .setURI(remoteRepository.toUri().toString())
        .setDirectory(updater.toFile())
        .call()) {
      Path target = updater.resolve(filePath);
      if (target.getParent() != null) {
        Files.createDirectories(target.getParent());
      }
      Files.writeString(target, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
      git.add().addFilepattern(filePath).call();
      git.commit().setMessage(message).call();
      git.push().call();
    }
  }

  private Path resolveLocalRepositoryPath(UUID userId) {
    Path root = workspaceRoot();
    if (root == null) {
      throw new IllegalStateException("workspaceRoot was not initialized");
    }
    return root.resolve(userId.toString()).resolve(REPOSITORY_NAME);
  }

  private void scheduleWorkspaceCleanup(UUID userId) {
    Path repository = resolveLocalRepositoryPath(userId);
    cleanupTargets.add(repository);
    Path userWorkspace = repository.getParent();
    if (userWorkspace != null) {
      cleanupTargets.add(userWorkspace);
    }
  }

  private Path workspaceRoot() {
    return (Path) ReflectionTestUtils.getField(githubIntegrationService, "workspaceRoot");
  }

  private String readSegmentContent(TextNoteSegment segment) {
    TextNoteSegment reloaded = textNoteSegmentRepository.findById(segment.getId()).orElseThrow();
    return reloaded.getContent();
  }

  private File findImportedRepositoryDirectory(UserData user) {
    File rootDirectory = fileRepository.getRootDirectory(user);
    return fileRepository.findFilesByDirectory(rootDirectory).stream()
        .filter(file -> file.getName().equals(REPOSITORY_NAME))
        .findFirst()
        .orElseThrow();
  }
}