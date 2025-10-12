package moanote.backend.service;

import jakarta.persistence.FlushModeType;
import jakarta.transaction.Transactional;
import moanote.backend.dto.CollaboratorDTO;
import moanote.backend.dto.FileCreateDTO;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.FileEditDTO;
import moanote.backend.dto.ShareFileDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.FileUserData;
import moanote.backend.entity.FileUserData.Permission;
import moanote.backend.entity.Note.NoteType;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.FileUserDataRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import moanote.backend.entity.Note;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class FileService {

  private final FileRepository fileRepository;

  private final UserDataRepository userDataRepository;

  private final FileUserDataRepository fileUserDataRepository;

  private final NoteService noteService;

  @Autowired
  public FileService(FileRepository fileRepository, UserDataRepository userDataRepository,
      FileUserDataRepository fileUserDataRepository, NoteService noteService) {
    this.fileRepository = fileRepository;
    this.userDataRepository = userDataRepository;
    this.fileUserDataRepository = fileUserDataRepository;
    this.noteService = noteService;
  }

  /**
   * <pre>
   * 새로운 file 을 생성하고, 생성 요청을 한 유저에게 파일에 대한 OWNER 권한을 부여합니다.
   * 파라미터의 유효성 체크는 caller 가 수행해야 합니다.
   * </pre>
   *
   * @param creator 생성자 id
   * @param filename  생성할 파일의 이름
   * @param type      생성할 파일의 성격
   * @param directory 생성된 파일이 위치할 디렉토리
   * @return 생성된 File entity
   */
  @Transactional
  protected File doCreateFile(UserData creator, String filename, FileType type, File directory) {
    File newFile = fileRepository.createFile(filename, type, directory);
    FileUserData permission = fileUserDataRepository.createFileUserData(creator, newFile, FileUserData.Permission.OWNER);

    if (type == FileType.DOCUMENT) {
      noteService.createNote(creator.getId(), newFile);
    }
    directory.addChild(newFile);
    return newFile;
  }

  /**
   * 새로운 file 을 생성하고, 생성 요청을 한 유저에게 파일에 대한 OWNER 권한을 부여합니다.
   *
   * @param creatorId   생성자 id
   * @param filename    생성할 파일의 이름
   * @param type        생성할 파일의 성격
   * @param directoryId 생성된 파일이 위치할 디렉토리의 id
   * @return 생성된 File entity
   */
  @Transactional
  public File createFile(UUID creatorId, String filename, FileType type, UUID directoryId) {
    UserData creator = userDataRepository.findById(creatorId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + creatorId));
    File directory = fileRepository.findFileById(directoryId).orElseThrow(
        () -> new NoSuchElementException("Directory not found with id: " + directoryId));
    if (directory.getType() != FileType.DIRECTORY) {
      throw new NoSuchElementException("Provided file is not a directory");
    }
    if (!hasAnyPermission(directory.getId(), creatorId)) {
      throw new IllegalArgumentException("User does not have permission to create file in this directory");
    }
    return doCreateFile(creator, filename, type, directory);
  }

  /**
   * 새로운 file 을 생성하고, 생성 요청을 한 유저에게 파일에 대한 OWNER 권한을 부여합니다. 파일이 루트 디렉토리에 생성됩니다.
   *
   * @param creatorId 생성자 id
   * @param filename  생성할 파일의 이름
   * @param type      생성할 파일의 성격
   * @return 생성된 File entity
   */
  @Transactional
  public File createFile(UUID creatorId, String filename, FileType type) {
    UserData creator = userDataRepository.findById(creatorId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + creatorId));
    return doCreateFile(creator, filename, type, fileRepository.getRootDirectory(creator));
  }

  @Transactional
  public FileDTO createFile(UUID directoryId, UUID userId, FileCreateDTO fileCreateDTO) {
    UserData creator = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    File file;
    if (directoryId == null)
      file = createFile(userId, fileCreateDTO.name(), fileCreateDTO.type());
    else
      file = createFile(userId, fileCreateDTO.name(), fileCreateDTO.type(), directoryId);

    if (fileCreateDTO.isCode()) {
      file.getNote().setType(NoteType.CODE);
      file.getNote().setCodeLanguage(fileCreateDTO.language());
    }
    return new FileDTO(file, creator);
  }

  /**
   * createRootDirectory 는 유저의 루트 디렉토리를 생성합니다. 이미 루트 디렉토리가 존재하는 경우에는 아무 작업도 하지 않습니다.
   *
   * @param userId 루트 디렉토리를 생성할 유저의 id
   * @return 생성된 루트 디렉토리 File entity
   */
  public File createRootDirectory(UUID userId) {
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    try {
      return fileRepository.getRootDirectory(user);
    } catch (NoSuchElementException e) {
      // 루트 디렉토리가 존재하지 않는 경우에만 생성
      File rootDirectory = fileRepository.createRootDirectory();
      FileUserData permission = fileUserDataRepository.createFileUserData(user, rootDirectory,
          FileUserData.Permission.OWNER);
      return rootDirectory;
    }
  }

  /**
   * 특정 File 의 특정 User 가 가진 permission 을 생성, 혹은 이미 존재하는 경우 permission 을 업데이트합니다.
   *
   * @param fileId     permission 을 부여할 File 의 id
   * @param userId     permission 을 부여할 User 의 id
   * @param permission 부여할 permission
   * @return permission 을 부여한 File entity
   */
  @Transactional
  public File grantPermission(UUID fileId, UUID userId, FileUserData.Permission permission) {
    File file = fileRepository.findById(fileId).orElseThrow();
    UserData userData = userDataRepository.findById(userId).orElseThrow();
    FileUserData oldPermission = fileUserDataRepository
        .findByFileAndUser(file, userData)
        .orElse(null);

    if (oldPermission == null) {
      fileUserDataRepository.createFileUserData(userData, file, permission);
      return file;
    }

    if (oldPermission.getPermission() == FileUserData.Permission.OWNER) {
      throw new IllegalArgumentException("Cannot change permission of owner");
    }
    oldPermission.setPermission(permission);
    fileUserDataRepository.save(oldPermission);
    return file;
  }

  /**
   * fileId 에 해당하는 File 검색
   *
   * @param fileId 찾을 File 의 id
   * @return 찾아진 File entity 객체
   * @throws NoSuchElementException fileId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public FileDTO getFileById(UUID fileId, UUID userId) {
    if (!hasAnyPermission(fileId, userId)) {
      throw new IllegalArgumentException("User does not have permission to access this file");
    }

    File file = fileRepository.findById(fileId).orElseThrow();
    return new FileDTO(file, fileUserDataRepository.findOwnerByFile(file).getUser());
  }

  /**
   * fileId 에 해당하는 File 의 name 을 업데이트합니다.
   *
   * @param fileId  업데이트할 File 의 id
   * @param newName file 의 새로운 이름
   * @return fileId 에 해당하는 File entity
   * @throws NoSuchElementException fileId 에 해당하는 객체를 찾을 수 없는 경우
   */
  public File updateFileName(UUID fileId, String newName) {
    return fileRepository.updateName(fileRepository.findFileById(fileId).orElseThrow(), newName);
  }

  /**
   * fileId 에 해당하는 File 의 위치를 업데이트합니다.
   *
   * @param fileId         업데이트할 File 의 id
   * @param newDirectoryId file 의 새로운 위치가 될 디렉토리의 id
   * @return fileId 에 해당하는 File entity
   * @throws NoSuchElementException 각 파라미터의 id 에 해당하는 객체를 찾을 수 없는 경우
   */
  public File moveFile(UUID fileId, UUID newDirectoryId) {
    File file = fileRepository.findFileById(fileId)
        .orElseThrow(() -> new NoSuchElementException("File not found with id: " + fileId));
    File newDirectory = fileRepository.findFileById(newDirectoryId)
        .orElseThrow(
            () -> new NoSuchElementException("Directory not found with id: " + newDirectoryId));
    return fileRepository.moveToDirectory(file, newDirectory);
  }

  public List<File> getFilesByOwnerUserId(UUID userId) {
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    return fileRepository.findFilesByOwner(user);
  }

  /**
   * userId 와 연관된 모든 file 를 가져옵니다.
   *
   * @param userId userId
   * @return userId와 연관된 모든 files
   */
  public List<File> getFilesByUserId(UUID userId) {
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    return fileRepository.findFilesByUser(user);
  }

  public List<FileDTO> getFileDTOByUserId(UUID userId) {
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    return fileRepository.findFilesByUser(user).stream().map(file ->
        new FileDTO(file, fileUserDataRepository.findOwnerByFile(file).getUser())).toList();
  }

  /**
   * <pre>
   * 파일을 재귀적으로 탐색합니다. 만약 파일이 디렉토리인 경우 하위 파일들을 모두 재귀적으로 탐색하는데, 후위 순회로 탐색합니다.
   * 같은 형제 노드 관계의 파일 사이에서는 임의의 순서로 방문합니다.
   * </pre>
   *
   * @param file    탐색을 시작할 File 객체
   * @param onVisit 각 파일을 방문할 때마다 해당 파일에 대해 호출되는 Consumer 함수. 후위 순회 순서로 onVisit 을 처리합니다.
   */
  @Transactional
  public void traverseFilesRecursively(File file, Consumer<File> onVisit) {
    if (file.getType() == FileType.DIRECTORY) {
      Stack<ArrayList<File>> subFilesStack = new Stack<>();
      Stack<Integer> resolvedSubFilesCountStack = new Stack<>();
      subFilesStack.push(new ArrayList<>(fileRepository.findFilesByDirectory(file)));
      resolvedSubFilesCountStack.push(0);

      while (!subFilesStack.isEmpty()) {
        ArrayList<File> subFiles = subFilesStack.peek();
        Integer resolvedSubFilesCount = resolvedSubFilesCountStack.pop();

        if (resolvedSubFilesCount == subFiles.size()) {
          for (File subFile : subFiles) {
            onVisit.accept(subFile);
          }
          subFilesStack.pop();
          continue;
        }

        while (resolvedSubFilesCount < subFiles.size()
            && subFiles.get(resolvedSubFilesCount).getType() != FileType.DIRECTORY) {
          resolvedSubFilesCount++;
        }

        if (resolvedSubFilesCount == subFiles.size()) {
          resolvedSubFilesCountStack.push(resolvedSubFilesCount);
          continue;
        }

        resolvedSubFilesCountStack.push(resolvedSubFilesCount + 1);
        File subFile = subFiles.get(resolvedSubFilesCount);
        subFilesStack.push(new ArrayList<>(fileRepository.findFilesByDirectory(subFile)));
        resolvedSubFilesCountStack.push(0);
      }
    }

    onVisit.accept(file);
  }

  /**
   * 특정 directory 아래의 모든 파일을 불러옵니다. 요청한 directory 를 포함하여 반환합니다.
   *
   * @param directoryId 디렉토리의 id null 이면 루트 디렉토리로 간주합니다.
   * @return 해당 디렉토리 및 디렉토리 아래의 모든 파일 리스트
   */
  @Transactional
  public List<FileDTO> getFilesInDirectory(UUID directoryId, UUID userId, boolean recursive) {
    File directory;
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    if (directoryId == null) {
      directory = fileRepository.getRootDirectory(user);
    } else {
      directory = fileRepository.findFileById(directoryId).orElseThrow();
    }

    if (directoryId != null && directory.getType() != FileType.DIRECTORY) {
      throw new IllegalArgumentException("Provided file is not a directory");
    }

    if (directoryId != null && !hasAnyPermission(directory.getId(), userId)) {
      throw new IllegalArgumentException("User does not have permission to access this directory");
    }

    if (!recursive) {
      List<File> files = fileRepository.findFilesByDirectory(directory);
      files.add(directory);
      return files.stream()
          .map(file -> new FileDTO(file, fileUserDataRepository.findOwnerByFile(file).getUser()))
          .toList();
    }

    LinkedList<FileDTO> files = new LinkedList<>();
    Consumer<File> onVisit = file -> files.add(
        new FileDTO(file, fileUserDataRepository.findOwnerByFile(file).getUser()));
    traverseFilesRecursively(directory, onVisit);

    return files;
  }

  /**
   * 파일을 삭제합니다. 이때, 해당 파일에 대한 모든 FileUserData 를 먼저 삭제합니다. file 이 directory 라면 해당 directory 아래의 모든
   * 파일을 재귀적으로 삭제합니다.
   *
   * @param fileId 삭제할 디렉토리
   * @param userId 삭제를 요청한 유저의 id
   */
  @Transactional
  public void deleteFile(UUID fileId, UUID userId) {
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new NoSuchElementException("File not found with id: " + fileId));
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

    if (file.getDirectory() == null) {
      throw new IllegalArgumentException("Cannot delete root directory");
    }

    if (!hasAnyPermission(fileId, user.getId())) {
      throw new IllegalArgumentException("User does not have permission to delete this file");
    }
    traverseFilesRecursively(file, this::deleteFileNode);
  }

  private void deleteFileNode(File file) {
    Note note = file.getNote();
    if (note != null) {
      UUID noteId = note.getId();
      note.setFile(null);
      file.setNote(null);
      noteService.deleteNote(noteId);
    }
    UUID fileId = file.getId();
    fileUserDataRepository.deleteAllByFileId(fileId);
    fileRepository.deleteById(fileId);
  }

  /**
   * 특정 파일에 대해 특정 유저가 특정 permission 을 가지고 있는지 확인합니다.
   *
   * @param fileId     검사할 file 의 id
   * @param userId     검사할 user 의 id
   * @param permission 검사할 permission
   * @return 해당 유저가 해당 파일에 대해 해당 permission 을 가지고 있는지 여부
   */
  public boolean hasPermission(UUID fileId, UUID userId, FileUserData.Permission permission) {
    File file = fileRepository.findById(fileId).orElseThrow();
    UserData userData = userDataRepository.findById(userId).orElseThrow();
    return fileUserDataRepository
        .findByFileAndUser(file, userData)
        .map(fileUserData -> fileUserData.getPermission() == permission)
        .orElse(false);
  }

  /**
   * 특정 파일에 대해 특정 유저가 permission 을 가지고 있는지 확인합니다. permission 이 무엇이든 상관없습니다. 만약 특정 권한을 가지는 있는 지를 확인하고
   * 싶다면, hasPermission(fileId, userId, permission) 메소드를 사용하세요.
   *
   * @param fileId 검사할 file 의 id
   * @param userId 검사할 user 의 id
   * @return 해당 유저가 해당 파일에 대해 어떤 permission 이든 가지고 있는지 여부
   * @see #hasPermission(UUID, UUID, FileUserData.Permission)
   */
  public boolean hasAnyPermission(UUID fileId, UUID userId) {
    File file = fileRepository.findById(fileId).orElseThrow();
    UserData userData = userDataRepository.findById(userId).orElseThrow();
    return fileUserDataRepository
        .findByFileAndUser(file, userData)
        .isPresent();
  }

  @Transactional
  public FileDTO editFile(UUID userId, UUID fileId, FileEditDTO fileEditDTO) {
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new NoSuchElementException("File not found with id: " + fileId));
    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

    if (fileEditDTO.name() != null) {
      file.setName(fileEditDTO.name());
    }

    if (!hasAnyPermission(fileId, userId)) {
      throw new IllegalArgumentException("User does not have permission to edit this file");
    }

    if (fileEditDTO.dir() == null) {
      fileEditDTO = new FileEditDTO(fileEditDTO.name(), fileRepository.getRootDirectory(user).getId());
    }

    UUID newDirectoryId = fileEditDTO.dir();
    file.setDirectory(fileRepository.findFileById(newDirectoryId)
        .orElseThrow(() -> new NoSuchElementException("Directory not found with id: " + newDirectoryId)));

    if (!hasAnyPermission(newDirectoryId, userId)) {
      throw new IllegalArgumentException("User does not have permission to move this file to the specified directory");
    }

    UserData owner = fileUserDataRepository.findOwnerByFile(file).getUser();
    fileRepository.save(file);
    return new FileDTO(file, owner);
  }

  @Transactional
  public void shareFile(UUID fileId, UUID userId, ShareFileDTO shareFileDTO) {
    File file = fileRepository.findFileById(fileId)
        .orElseThrow(() -> new NoSuchElementException("File not found : " + fileId));
    UserData requester = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("Requester not found : " + userId));
    if (!hasPermission(fileId, userId, Permission.OWNER)) {
      throw new IllegalArgumentException(
          "Requester is not owner : user=" + userId + " file=" + fileId);
    }
    UserData targetUser = userDataRepository.findByUsername(shareFileDTO.username())
        .orElseThrow(() -> new NoSuchElementException("Target user not found : " + shareFileDTO.username()));

    traverseFilesRecursively(file, (f) -> {
      grantPermission(f.getId(), targetUser.getId(), shareFileDTO.permission());
    });
  }

  @Transactional
  public List<CollaboratorDTO> getCollaborators(UUID fileId, UUID requestUserId) {
    File file = fileRepository.findFileById(fileId)
        .orElseThrow(() -> new NoSuchElementException("File not found : " + fileId));
    UserData requester = userDataRepository.findById(requestUserId)
        .orElseThrow(() -> new NoSuchElementException("Requester not found : " + requestUserId));
    if (!hasAnyPermission(fileId, requestUserId)) {
      throw new IllegalArgumentException(
          "Requester has no permission : user=" + requestUserId + " file=" + fileId);
    }
    return fileUserDataRepository.findByFile(file).stream().map(CollaboratorDTO::new).toList();
  }
}