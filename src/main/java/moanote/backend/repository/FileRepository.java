package moanote.backend.repository;

import com.github.f4b6a3.uuid.UuidCreator;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface FileRepository extends JpaRepository<File, UUID> {
  @Query(value = """
      SELECT n
      FROM File n
      JOIN FileUserData fud ON n.id = fud.file.id
      WHERE fud.user.id = :owner
      AND fud.permission = 'OWNER'
      """)
  List<File> findFilesByOwner(@Param("owner") UUID ownerUserId);

  @Query(value = """
      SELECT n
      FROM File n
      JOIN FileUserData fud ON n.id = fud.file.id
      WHERE fud.user.id = :user
      """)
  List<File> findFilesByUser(@Param("user") UUID ownerUserId);

  List<File> findFilesByDirectory(File directory);

  Optional<File> findFileById(UUID id);

  /**
   * 특정 유저의 루트 디렉토리를 반환합니다.
   *
   * @param ownerUserId 유저의 ID
   * @return 해당 유저의 루트 디렉토리
   */
  default File getRootDirectory(UUID ownerUserId) {
    return findFilesByUser(ownerUserId).stream()
        .filter(file -> file.getType() == FileType.DIRECTORY && file.getDirectory() == null)
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Root directory not found for user: " + ownerUserId));
  }

  default File createFile(String name, FileType type, File directory) {
    File file = new File();
    file.setName(name);
    file.setId(UuidCreator.getTimeOrderedEpoch());
    file.setType(type);
    file.setDirectory(directory);
    return save(file);
  }

  default File createRootDirectory() {
    File file = new File();
    file.setName(".");
    file.setId(UuidCreator.getTimeOrderedEpoch());
    file.setType(FileType.DIRECTORY);
    file.setDirectory(null);
    return save(file);
  }

  /**
   * File 의 name 를 newName 로 덮어씁니다.
   *
   * @param file    업데이트할 File
   * @param newName 저장할 content
   * @return 업데이트 대상이 된 File 엔티티
   * @throws NoSuchElementException file.id 에 해당하는 데이터가 존재하지 않을 경우
   */
  default File updateName(File file, String newName) {
    findById(file.getId()).orElseThrow();
    file.setName(newName);
    return save(file);
  }

  /**
   * File 이 위치한 디렉토리를 변경합니다.
   *
   * @param file      업데이트할 File
   * @param directory 새롭게 위치할 디렉토리
   * @return 업데이트 대상이 된 File 엔티티
   * @throws NoSuchElementException file.id 혹은 directory.id 에 해당하는 데이터가 존재하지 않을 경우
   */
  default File moveToDirectory(File file, File directory) {
    findById(file.getId()).orElseThrow();
    findById(directory.getId()).orElseThrow();
    file.setDirectory(directory);
    return save(file);
  }
}
