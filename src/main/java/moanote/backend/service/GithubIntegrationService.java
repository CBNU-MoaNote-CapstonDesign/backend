package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.GithubCredentials;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.Note;
import moanote.backend.entity.Note.CodeLanguage;
import moanote.backend.entity.Note.NoteType;
import moanote.backend.entity.TextNoteSegment;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.UserDataRepository;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;


/**
 * <pre>
 *   GithubIntegrationService 는 GitHub 저장소와 상호작용하여 애플리케이션의 File/Note 구조와 동기화하는 기능을 제공합니다.
 * </pre>
 */
@Service
public class GithubIntegrationService {

  private static final Set<String> TEXTUAL_EXTENSIONS = Set.of(".txt", ".md", ".java", ".py", ".cs", ".js", ".jsx",
      ".ts", ".tsx", ".json", ".yml", ".yaml", ".xml", ".html", ".css", ".sql", ".gradle", ".properties");

  private final FileService fileService;
  private final NoteService noteService;
  private final UserDataRepository userDataRepository;
  private final GithubTokenService githubTokenService;
  private final FileRepository fileRepository;
  private final TextNoteSegmentRepository textNoteSegmentRepository;
  private final Path workspaceRoot;

  public GithubIntegrationService(FileService fileService, NoteService noteService,
      UserDataRepository userDataRepository, FileRepository fileRepository,
      TextNoteSegmentRepository textNoteSegmentRepository, GithubTokenService githubTokenService) {
    this.fileService = fileService;
    this.noteService = noteService;
    this.userDataRepository = userDataRepository;
    this.fileRepository = fileRepository;
    this.textNoteSegmentRepository = textNoteSegmentRepository;
    this.githubTokenService = githubTokenService;
    this.workspaceRoot = initializeWorkspaceRoot();
  }

  /**
   * <pre>
   *   GitHub 저장소를 사용자 전용 작업 공간에 클론한 뒤 텍스트 파일을 애플리케이션의 File/Note 구조로 가져옵니다.
   *   텍스트 파일이 아닌 경우 DB에 추가하지 않고 건너뜁니다. 클론된 저장소는 이후 브랜치 생성 및 동기화를 위해 유지됩니다.
   * </pre>
   *
   * @param userId        파일을 생성할 사용자
   * @param repositoryUrl 가져올 GitHub 저장소 URL
   * @param credentials   인증 정보 (nullable)
   * @return 생성된 문서 파일의 DTO 리스트
   */
  @Transactional
  public List<FileDTO> importRepository(UUID userId, String repositoryUrl) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");

    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

    GithubCredentials credentials = githubTokenService.findCredentials(userId)
        .orElse(GithubCredentials.anonymous());

    String repositoryName = resolveRepositoryName(repositoryUrl);
    Path cloneDirectory = prepareLocalRepository(userId, repositoryName);
    CredentialsProvider provider = createCredentialsProvider(credentials);

    try (Git git = Git.cloneRepository()
        .setURI(repositoryUrl)
        .setDirectory(cloneDirectory.toFile())
        .setCredentialsProvider(provider)
        .call()) {

      File userRootDirectory = fileRepository.getRootDirectory(user);
      File repositoryDirectory = fileService.createFile(userId, repositoryName, FileType.DIRECTORY,
          userRootDirectory.getId());
      repositoryDirectory.setGithubImported(true);

      Map<Path, File> directoryMapping = new HashMap<>();
      directoryMapping.put(cloneDirectory, repositoryDirectory);

      List<FileDTO> createdDocuments = new ArrayList<>();

      try (var paths = Files.walk(cloneDirectory)) {
        paths.sorted(Comparator.comparingInt(Path::getNameCount))
            .forEach(path -> handlePath(userId, user, path, cloneDirectory, directoryMapping, createdDocuments));
      } catch (IOException e) {
        throw new IllegalStateException("Failed to traverse repository files", e);
      }

      return createdDocuments;

    } catch (GitAPIException e) {
      deleteDirectoryQuietly(cloneDirectory);
      throw new IllegalStateException("Failed to clone repository", e);
    }
  }

  /**
   * <pre>
   *   클론된 저장소의 개별 경로를 순회하면서 디렉터리 매핑을 유지하고 텍스트 파일을 DB 엔터티로 변환합니다.
   * </pre>
   *
   * @param userId            파일 소유자
   * @param owner             파일 소유자 엔터티
   * @param currentPath       현재 순회 중인 경로
   * @param cloneDirectory    클론된 저장소의 루트 경로
   * @param directoryMapping  로컬 경로와 DB 디렉터리 매핑
   * @param createdDocuments  생성된 문서 DTO 누적 리스트
   */
  private void handlePath(UUID userId, UserData owner, Path currentPath, Path cloneDirectory,
      Map<Path, File> directoryMapping, List<FileDTO> createdDocuments) {
    processRepositoryEntry(cloneDirectory, currentPath, directoryMapping,
        (parentDirectory, path) -> {
          File importedDirectory = fileService.createFile(userId, path.getFileName().toString(),
              FileType.DIRECTORY,
              parentDirectory.getId());
          importedDirectory.setGithubImported(true);
          return importedDirectory;
        },
        (parentDirectory, path) -> {
          String filename = path.getFileName().toString();
          File importedFile = fileService.createFile(userId, filename, FileType.DOCUMENT, parentDirectory.getId());
          importedFile.setGithubImported(true);
          Note note = importedFile.getNote();
          CodeLanguage language = determineLanguage(filename);
          if (language != CodeLanguage.TEXT) {
            note.setType(NoteType.CODE);
          }
          note.setCodeLanguage(language);

          populateNoteContent(importedFile, readFileContent(path));
          createdDocuments.add(new FileDTO(importedFile, owner));
        });
  }

  /**
   * <pre>
   *   UTF-8 인코딩으로 파일 내용을 읽어 문자열로 반환합니다.
   * </pre>
   *
   * @param currentPath 읽을 파일 경로
   * @return 파일의 문자열 콘텐츠
   */
  private String readFileContent(Path currentPath) {
    try {
      return Files.readString(currentPath, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read file content: " + currentPath, e);
    }
  }

  /**
   * <pre>
   *   확장자 및 MIME 타입을 기반으로 텍스트 파일인지 여부를 판단합니다.
   * </pre>
   *
   * @param path 검사할 파일 경로
   * @return 텍스트 파일일 경우 true
   */
  private boolean isTextualFile(Path path) {
    String filename = path.getFileName().toString();
    String lowerName = filename.toLowerCase();
    if (TEXTUAL_EXTENSIONS.stream().anyMatch(lowerName::endsWith)) {
      return true;
    }
    try {
      String probe = Files.probeContentType(path);
      return probe != null && probe.startsWith("text");
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * <pre>
   *   CRDT 노드 구조에 문자열 콘텐츠를 삽입하여 노트 내용을 구성합니다.
   * </pre>
   *
   * @param importedFile 대상 파일 엔터티
   * @param content      삽입할 텍스트 콘텐츠
   */
  private void populateNoteContent(File importedFile, String content) {
    if (content == null) {
      content = "";
    }
    TextNoteSegment segment = noteService.createTextNoteSegment(importedFile.getNote().getId());
    segment.updateContent(content);
    textNoteSegmentRepository.saveAndFlush(segment);
  }

  /**
   * <pre>
   *   파일 확장자를 기반으로 노트의 코드 언어를 결정합니다.
   * </pre>
   *
   * @param filename 언어 판별 대상 파일명
   * @return 매칭되는 {@link CodeLanguage}
   */
  private CodeLanguage determineLanguage(String filename) {
    String lower = filename.toLowerCase();
    if (lower.endsWith(".java")) {
      return CodeLanguage.JAVA;
    }
    if (lower.endsWith(".py")) {
      return CodeLanguage.PYTHON;
    }
    if (lower.endsWith(".cs")) {
      return CodeLanguage.CSHARP;
    }
    if (lower.endsWith(".tsx")) {
      return CodeLanguage.TYPESCRIPT_JSX;
    }
    if (lower.endsWith(".ts")) {
      return CodeLanguage.TYPESCRIPT;
    }
    if (lower.endsWith(".jsx")) {
      return CodeLanguage.JAVASCRIPT_JSX;
    }
    if (lower.endsWith(".js")) {
      return CodeLanguage.JAVASCRIPT;
    }
    return CodeLanguage.TEXT;
  }

  /**
   * <pre>
   *   Git 작업에 사용할 CredentialsProvider 를 생성합니다.
   * </pre>
   *
   * @param credentials 사용자 인증 정보
   * @return JGit CredentialsProvider 인스턴스 또는 null
   */
  private CredentialsProvider createCredentialsProvider(GithubCredentials credentials) {
    if (credentials == null || !credentials.isPresent()) {
      return null;
    }
    String username = credentials.username() != null ? credentials.username() : "oauth2";
    return new UsernamePasswordCredentialsProvider(username, credentials.token());
  }

  /**
   * <pre>
   *   저장소 URL 에서 디렉터리 이름으로 사용할 저장소 이름을 추출합니다.
   * </pre>
   *
   * @param repositoryUrl 원격 저장소 URL
   * @return 추출된 저장소 이름
   */
  private String resolveRepositoryName(String repositoryUrl) {
    try {
      URI uri = URI.create(repositoryUrl);
      String path = uri.getPath();
      if (path != null && !path.isBlank()) {
        String sanitized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int slashIndex = sanitized.lastIndexOf('/');
        if (slashIndex >= 0) {
          sanitized = sanitized.substring(slashIndex + 1);
        }
        if (sanitized.endsWith(".git")) {
          sanitized = sanitized.substring(0, sanitized.length() - 4);
        }
        if (!sanitized.isBlank()) {
          return sanitized;
        }
      }
    } catch (IllegalArgumentException ignored) {
      // fall back to string heuristics
    }

    String sanitized = repositoryUrl.trim();
    if (sanitized.endsWith(".git")) {
      sanitized = sanitized.substring(0, sanitized.length() - 4);
    }
    int slashIndex = sanitized.lastIndexOf('/');
    if (slashIndex >= 0) {
      sanitized = sanitized.substring(slashIndex + 1);
    }
    int colonIndex = sanitized.lastIndexOf(':');
    if (colonIndex >= 0) {
      sanitized = sanitized.substring(colonIndex + 1);
    }
    return sanitized.isEmpty() ? "repository" : sanitized;
  }

  /**
   * <pre>
   *   임시 작업 디렉터리를 재귀적으로 삭제하되 실패 시 무시합니다.
   * </pre>
   *
   * @param directory 삭제할 디렉터리 경로
   */
  private void deleteDirectoryQuietly(Path directory) {
    if (directory == null) {
      return;
    }
    try {
      if (Files.notExists(directory)) {
        return;
      }
      try (var paths = Files.walk(directory)) {
        paths.sorted(Comparator.reverseOrder())
            .forEach(path -> {
              try {
                Files.deleteIfExists(path);
              } catch (IOException ignored) {
                // best effort cleanup
              }
            });
      }
    } catch (IOException ignored) {
      // noop
    }
  }

  /**
   * <pre>
   *   로컬 작업 공간에 저장된 저장소에서 새로운 브랜치를 생성하고 커밋 및 푸시를 수행합니다.
   * </pre>
   *
   * @param userId         로컬 저장소를 식별하기 위한 사용자 ID
   * @param repositoryUrl  원격 저장소 URL
   * @param baseBranch     새 브랜치의 기준이 될 브랜치 이름
   * @param branchName     생성할 브랜치 이름
   * @param commitMessage  커밋 메시지
   * @param filesToCommit  커밋에 포함될 파일 내용 (경로, 내용)
   */
  public void createBranchAndCommit(UUID userId, String repositoryUrl, String baseBranch, String branchName,
      String commitMessage, Map<String, String> filesToCommit) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");
    Objects.requireNonNull(baseBranch, "baseBranch must not be null");
    Objects.requireNonNull(branchName, "branchName must not be null");
    Objects.requireNonNull(commitMessage, "commitMessage must not be null");

    GithubCredentials credentials = githubTokenService.findCredentials(userId)
        .orElse(GithubCredentials.anonymous());
    CredentialsProvider provider = createCredentialsProvider(credentials);
    Path repositoryPath = resolveExistingRepositoryPath(userId, repositoryUrl);

    try (Git git = Git.open(repositoryPath.toFile())) {
      updateBaseBranch(git, baseBranch, provider);
      boolean branchExists;
      try {
        branchExists = git.getRepository().findRef("refs/heads/" + branchName) != null;
      } catch (IOException e) {
        throw new IllegalStateException("Failed to inspect existing branches", e);
      }
      if (branchExists) {
        git.branchDelete().setBranchNames(branchName).setForce(true).call();
      }
      git.checkout().setCreateBranch(true).setName(branchName)
          .setStartPoint("origin/" + baseBranch)
          .call();

      if (filesToCommit != null && !filesToCommit.isEmpty()) {
        Path userWorkspace = workspaceRoot.resolve(userId.toString());
        for (Map.Entry<String, String> entry : filesToCommit.entrySet()) {
          Path workspaceRelative = Paths.get(entry.getKey());
          if (workspaceRelative.isAbsolute()) {
            throw new IllegalArgumentException("File paths must be relative to the user workspace");
          }
          Path filePath = userWorkspace.resolve(workspaceRelative).normalize();
          if (!filePath.startsWith(userWorkspace)) {
            throw new IllegalArgumentException("File path escapes user workspace: " + entry.getKey());
          }
          if (!filePath.startsWith(repositoryPath)) {
            throw new IllegalArgumentException("File path must target the repository: " + entry.getKey());
          }
          if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
          }
          Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING);
          String repositoryRelativePath = repositoryPath.relativize(filePath).toString().replace("\\", "/");
          git.add().addFilepattern(repositoryRelativePath).call();
        }
      }

      git.commit().setMessage(commitMessage).setAllowEmpty(true).call();
      git.push().setCredentialsProvider(provider).add("refs/heads/" + branchName).call();
    } catch (GitAPIException | IOException e) {
      throw new IllegalStateException("Failed to create branch and commit", e);
    }
  }

  /**
   * <pre>
   *   로컬 저장소에서 원격 저장소의 변경 사항을 fetch 한 뒤 지정된 브랜치로 동기화합니다.
   * </pre>
   *
   * @param userId         저장소 소유자
   * @param repositoryUrl  원격 저장소 URL
   * @param branchName     동기화할 브랜치 이름
   */
  @Transactional
  public void fetchRepository(UUID userId, String repositoryUrl, String branchName) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");
    Objects.requireNonNull(branchName, "branchName must not be null");
    GithubCredentials credentials = githubTokenService.findCredentials(userId)
        .orElse(GithubCredentials.anonymous());
    CredentialsProvider provider = createCredentialsProvider(credentials);
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    String repositoryName = resolveRepositoryName(repositoryUrl);
    Path repositoryPath = resolveExistingRepositoryPath(userId, repositoryUrl);
    File repositoryDirectory = resolveRepositoryDirectory(user, repositoryName);
    try (Git git = Git.open(repositoryPath.toFile())) {
      git.fetch().setCredentialsProvider(provider).call();
      checkoutOrCreateBranch(git, branchName);
      git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
          .setRef("origin/" + branchName)
          .call();
      git.clean().setForce(true).setCleanDirectories(true).call();
      git.pull().setCredentialsProvider(provider).setRemoteBranchName(branchName).call();
    } catch (GitAPIException | IOException e) {
      throw new IllegalStateException("Failed to fetch repository", e);
    }
    synchronizeRepository(userId, repositoryPath, repositoryDirectory);
  }

  private Path initializeWorkspaceRoot() {
    Path root = Paths.get(System.getProperty("java.io.tmpdir"), "moanote-github-workspace");
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to prepare GitHub workspace", e);
    }
    return root;
  }

  private Path prepareLocalRepository(UUID userId, String repositoryName) {
    Path userWorkspace = workspaceRoot.resolve(userId.toString());
    try {
      Files.createDirectories(userWorkspace);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to prepare user workspace", e);
    }
    Path repositoryPath = userWorkspace.resolve(repositoryName);
    deleteDirectoryQuietly(repositoryPath);
    try {
      Files.createDirectories(repositoryPath);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to prepare repository directory", e);
    }
    return repositoryPath;
  }

  private Path resolveExistingRepositoryPath(UUID userId, String repositoryUrl) {
    String repositoryName = resolveRepositoryName(repositoryUrl);
    Path repositoryPath = workspaceRoot.resolve(userId.toString()).resolve(repositoryName);
    if (Files.notExists(repositoryPath)) {
      throw new IllegalArgumentException("Local repository not found at " + repositoryPath);
    }
    return repositoryPath;
  }

  private void updateBaseBranch(Git git, String baseBranch, CredentialsProvider provider)
      throws GitAPIException {
    git.fetch().setCredentialsProvider(provider).call();
    checkoutOrCreateBranch(git, baseBranch);
    git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
        .setRef("origin/" + baseBranch)
        .call();
    git.clean().setForce(true).setCleanDirectories(true).call();
  }

  private void checkoutOrCreateBranch(Git git, String branchName) throws GitAPIException {
    boolean branchExists;
    try {
      branchExists = git.getRepository().findRef("refs/heads/" + branchName) != null;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to inspect local branches", e);
    }
    if (!branchExists) {
      git.checkout()
          .setCreateBranch(true)
          .setName(branchName)
          .setStartPoint("origin/" + branchName)
          .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
          .call();
    } else {
      git.checkout().setName(branchName).call();
    }
  }

  private void synchronizeRepository(UUID userId, Path repositoryPath, File repositoryDirectory) {
    Map<Path, File> directoryMapping = new HashMap<>();
    directoryMapping.put(repositoryPath, repositoryDirectory);
    Map<UUID, Map<String, File>> directoryChildrenCache = new HashMap<>();
    try (var paths = Files.walk(repositoryPath)) {
      paths.sorted(Comparator.comparingInt(Path::getNameCount))
          .forEach(currentPath -> synchronizePath(userId, repositoryPath, directoryMapping,
              directoryChildrenCache, currentPath));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to traverse repository files", e);
    }
  }

  private void synchronizePath(UUID userId, Path repositoryPath, Map<Path, File> directoryMapping,
      Map<UUID, Map<String, File>> directoryChildrenCache, Path currentPath) {
    processRepositoryEntry(repositoryPath, currentPath, directoryMapping,
        (parentDirectory, path) -> resolveDirectory(userId, parentDirectory, path.getFileName().toString(),
            directoryChildrenCache),
        (parentDirectory, path) -> {
          File document = resolveDocument(userId, parentDirectory, path.getFileName().toString(),
              directoryChildrenCache);
          Note note = document.getNote();
          CodeLanguage language = determineLanguage(document.getName());
          if (language == CodeLanguage.TEXT) {
            note.setType(NoteType.NORMAL);
          } else {
            note.setType(NoteType.CODE);
          }
          note.setCodeLanguage(language);
          resetTextSegments(note);
          populateNoteContent(document, readFileContent(path));
        });
  }

  private void processRepositoryEntry(Path repositoryRoot, Path currentPath, Map<Path, File> directoryMapping,
      BiFunction<File, Path, File> directoryHandler, BiConsumer<File, Path> fileHandler) {
    if (shouldSkipRepositoryEntry(repositoryRoot, currentPath)) {
      return;
    }

    File parentDirectory = directoryMapping.get(currentPath.getParent());
    if (parentDirectory == null) {
      return;
    }

    if (Files.isDirectory(currentPath)) {
      File directory = directoryHandler.apply(parentDirectory, currentPath);
      if (directory != null) {
        directoryMapping.put(currentPath, directory);
      }
      return;
    }

    if (!isTextualFile(currentPath)) {
      return;
    }

    fileHandler.accept(parentDirectory, currentPath);
  }

  private boolean shouldSkipRepositoryEntry(Path repositoryRoot, Path currentPath) {
    if (currentPath.equals(repositoryRoot)) {
      return true;
    }
    return currentPath.startsWith(repositoryRoot.resolve(".git"));
  }

  private File resolveRepositoryDirectory(UserData user, String repositoryName) {
    File root = fileRepository.getRootDirectory(user);
    return fileRepository.findFilesByDirectory(root).stream()
        .filter(file -> file.getName().equals(repositoryName) && file.getType() == FileType.DIRECTORY)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Repository directory not found for user: " + user.getId() + " name: " + repositoryName));
  }

  private File resolveDirectory(UUID userId, File parentDirectory, String directoryName,
      Map<UUID, Map<String, File>> directoryChildrenCache) {
    File existing = findChildInDirectory(parentDirectory, directoryName, directoryChildrenCache);
    if (existing != null) {
      if (existing.getType() != FileType.DIRECTORY) {
        throw new IllegalStateException("Existing file is not a directory: " + directoryName);
      }
      return existing;
    }
    File created = fileService.createFile(userId, directoryName, FileType.DIRECTORY, parentDirectory.getId());
    created.setGithubImported(true);
    directoryChildrenCache.computeIfAbsent(parentDirectory.getId(), key -> new HashMap<>())
        .put(directoryName, created);
    return created;
  }

  private File resolveDocument(UUID userId, File parentDirectory, String filename,
      Map<UUID, Map<String, File>> directoryChildrenCache) {
    File existing = findChildInDirectory(parentDirectory, filename, directoryChildrenCache);
    if (existing != null) {
      if (existing.getType() != FileType.DOCUMENT) {
        throw new IllegalStateException("Existing file is not a document: " + filename);
      }
      return existing;
    }
    File created = fileService.createFile(userId, filename, FileType.DOCUMENT, parentDirectory.getId());
    created.setGithubImported(true);
    directoryChildrenCache.computeIfAbsent(parentDirectory.getId(), key -> new HashMap<>())
        .put(filename, created);
    return created;
  }

  private File findChildInDirectory(File directory, String childName, Map<UUID, Map<String, File>> directoryChildrenCache) {
    Map<String, File> children = directoryChildrenCache.computeIfAbsent(directory.getId(),
        key -> fileRepository.findFilesByDirectory(directory).stream()
            .collect(Collectors.toMap(File::getName, file -> file, (left, right) -> left, HashMap::new)));
    return children.get(childName);
  }

  private void resetTextSegments(Note note) {
    List<TextNoteSegment> segments = textNoteSegmentRepository.findAllByNote(note);
    for (TextNoteSegment segment : segments) {
      note.getSegments().remove(segment);
      textNoteSegmentRepository.delete(segment);
    }
    textNoteSegmentRepository.flush();
  }
}