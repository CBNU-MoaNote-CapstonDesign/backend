package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.FileEditDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.FileUserData;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.FileUserDataRepository;
import moanote.backend.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  @Autowired
  public FileService(FileRepository fileRepository, UserDataRepository userDataRepository,
      FileUserDataRepository fileUserDataRepository) {
    this.fileRepository = fileRepository;
    this.userDataRepository = userDataRepository;
    this.fileUserDataRepository = fileUserDataRepository;
  }

  /**
   * 새로운 file 을 생성하고, 생성 요청을 한 유저에게 파일에 대한 OWNER 권한을 부여합니다.
   *
   * @param creatorId 생성자 id
   * @param filename  생성할 파일의 이름
   * @param type      생성할 파일의 성격
   * @param directory 생성된 파일이 위치할 디렉토리
   * @return 생성된 File entity
   */
  @Transactional
  public File createFile(UUID creatorId, String filename, FileType type, File directory) {
    if (directory.getType() != FileType.DIRECTORY) {
      throw new IllegalArgumentException("Provided file is not a directory");
    }
    File newFile = fileRepository.createFile(filename, type, directory);
    fileUserDataRepository.createFileUserData(userDataRepository.findById(creatorId).orElseThrow(),
        newFile,
        FileUserData.Permission.OWNER);
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
    return createFile(creatorId, filename, type,
        fileRepository.findFileById(directoryId).orElseThrow(
            () -> new NoSuchElementException("Directory not found with id: " + directoryId)));
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
    return createFile(creatorId, filename, type, fileRepository.getRootDirectory(creatorId));
  }

  /**
   * createRootDirectory 는 유저의 루트 디렉토리를 생성합니다. 이미 루트 디렉토리가 존재하는 경우에는 아무 작업도 하지 않습니다.
   *
   * @param userId permission 을 삭제할 User 의 id
   * @return 생성된 루트 디렉토리 File entity
   */
  public File createRootDirectory(UUID userId) {
    try {
      return fileRepository.getRootDirectory(userId);
    } catch (NoSuchElementException e) {
      // 루트 디렉토리가 존재하지 않는 경우에만 생성
      File rootDirectory = fileRepository.createRootDirectory();
      fileUserDataRepository.createFileUserData(userDataRepository.findById(userId).orElseThrow(),
          rootDirectory,
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
  public File getFileById(UUID fileId) {
    return fileRepository.findById(fileId).orElseThrow();
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
    return fileRepository.findFilesByOwner(userId);
  }

  /**
   * userId 와 연관된 모든 file 를 가져옵니다.
   *
   * @param userId userId
   * @return userId와 연관된 모든 files
   */
  public List<File> getFilesByUserId(UUID userId) {
    return fileRepository.findFilesByUser(userId);
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
    // 파일이 디렉토리인 경우, 하위 파일들을 먼저 삭제
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
   * 특정 directory 아래의 모든 파일을 불러옵니다
   *
   * @param directoryId 디렉토리의 id
   * @return 해당 디렉토리 아래의 모든 파일 리스트
   */
  @Transactional
  public List<FileDTO> getFilesByDirectory(UUID directoryId, boolean recursive) {
    File directory = fileRepository.findFileById(directoryId).orElseThrow();

    if (directory.getType() != FileType.DIRECTORY) {
      throw new IllegalArgumentException("Provided file is not a directory");
    }

    if (!recursive) {
      return fileRepository.findFilesByDirectory(directory).stream()
          .map(FileDTO::new)
          .toList();
    }

    LinkedList<FileDTO> files = new LinkedList<>();
    Consumer<File> onVisit = file -> files.add(new FileDTO(file));
    traverseFilesRecursively(directory, onVisit);
    return files;
  }

  /**
   * 파일을 삭제합니다. 이때, 해당 파일에 대한 모든 FileUserData 를 먼저 삭제합니다. file 이 directory 라면 해당 directory 아래의 모든
   * 파일을 재귀적으로 삭제합니다.
   *
   * @param file 삭제할 디렉토리
   */
  @Transactional
  public void removeFilesRecursively(File file) {
    traverseFilesRecursively(file, f -> {
      fileUserDataRepository.deleteAllByFileId(f.getId());
      fileRepository.delete(f);
    });
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
  public File editFile(UUID userId, UUID fileId, FileEditDTO fileEditDTO) {
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new NoSuchElementException("File not found with id: " + fileId));

    if (fileEditDTO.name() != null) {
      file.setName(fileEditDTO.name());
    }

    UserData user = userDataRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

    if (!hasAnyPermission(fileId, userId)) {
      throw new IllegalArgumentException("User does not have permission to edit this file");
    }

    if (fileEditDTO.dir() == null) {
      fileEditDTO = new FileEditDTO(fileEditDTO.name(), fileRepository.getRootDirectory(userId).getId());
    }

    UUID newDirectoryId = fileEditDTO.dir();
    file.setDirectory(fileRepository.findFileById(newDirectoryId)
        .orElseThrow(() -> new NoSuchElementException("Directory not found with id: " + newDirectoryId)));

    if (!hasAnyPermission(newDirectoryId, userId)) {
      throw new IllegalArgumentException("User does not have permission to move this file to the specified directory");
    }

    return fileRepository.save(file);
  }
}