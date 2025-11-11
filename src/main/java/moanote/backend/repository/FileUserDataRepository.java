package moanote.backend.repository;

import moanote.backend.entity.File;
import moanote.backend.entity.FileUserData.Permission;
import moanote.backend.entity.UserData;
import moanote.backend.entity.FileUserData;
import moanote.backend.entity.FileUserDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * File 과 User 사이 권한 정보 질의를 위한 레포지토리
 */
public interface FileUserDataRepository extends JpaRepository<FileUserData, FileUserDataId> {

  default FileUserData createFileUserData(UserData user, File file,
      FileUserData.Permission permission) {
    FileUserData fileUserData = new FileUserData();
    fileUserData.setUser(user);
    fileUserData.setFile(file);
    fileUserData.setPermission(permission);
    return save(fileUserData);
  }

  void deleteAllByFileId(UUID fileId);

  Optional<FileUserData> findByFileAndUser(File file, UserData user);

  List<FileUserData> findByFile(File file);

  @Query(value = """
      SELECT fud
      FROM FileUserData fud
      WHERE fud.file = :file AND fud.permission = 'OWNER'
      """)
  FileUserData findOwnerByFile(@Param("file") File file);
}
