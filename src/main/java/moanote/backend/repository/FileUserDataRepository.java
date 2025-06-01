package moanote.backend.repository;

import moanote.backend.entity.File;
import moanote.backend.entity.UserData;
import moanote.backend.entity.FileUserData;
import moanote.backend.entity.FileUserDataId;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
