package moanote.backend.service;

import jakarta.persistence.EntityManager;
import moanote.backend.BackendApplication;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import java.util.ArrayList;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class FileServiceTest {

  @Autowired
  private FileService fileService;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private EntityManager entityManager;


  @Test
  void removeFilesRecursively() {
    UserData userData = userService.createUser("testuser", "testpassword");
    File rootDirectory = fileRepository.getRootDirectory(userData);
    File subDirectory;
    ArrayList<File> subFilesAtRoot = new ArrayList<>();
    ArrayList<File> subFilesAtSubDirectory = new ArrayList<>();

    subFilesAtRoot.add(fileService.createFile(userData.getId(), "Doxyfile", FileType.DOCUMENT, rootDirectory.getId()));
    subFilesAtRoot.add(fileService.createFile(userData.getId(), "Makefile", FileType.DOCUMENT, rootDirectory.getId()));
    subFilesAtRoot.add(fileService.createFile(userData.getId(), "DockerFile", FileType.DOCUMENT, rootDirectory.getId()));
    subDirectory = fileService.createFile(userData.getId(), "src", FileType.DIRECTORY, rootDirectory.getId());
    subFilesAtRoot.add(subDirectory);
    subFilesAtSubDirectory.add(fileService.createFile(userData.getId(), "main.cpp", FileType.DOCUMENT, subDirectory.getId()));
    subFilesAtSubDirectory.add(fileService.createFile(userData.getId(), "service.cpp", FileType.DOCUMENT, subDirectory.getId()));
    subFilesAtSubDirectory.add(fileService.createFile(userData.getId(), "service.hpp", FileType.DOCUMENT, subDirectory.getId()));
    subFilesAtSubDirectory.add(fileService.createFile(userData.getId(), "service.cpp", FileType.DOCUMENT, subDirectory.getId()));

    subFilesAtSubDirectory.add(fileService.createFile(userData.getId(), "dir", FileType.DIRECTORY, subDirectory.getId()));
    File subSubDirectory = subFilesAtSubDirectory.getLast();
    subFilesAtSubDirectory.add(fileService.createFile(userData.getId(), "file.txt", FileType.DOCUMENT, subSubDirectory.getId()));

    fileService.deleteFile(subDirectory.getId(), userData.getId());
    for (File file : subFilesAtSubDirectory) {
      Assert.isTrue(!fileRepository.existsById(file.getId()), "File should be deleted: " + file.getName());
    }
    for (File file : subFilesAtRoot) {
      if (!file.equals(subDirectory)) {
        Assert.isTrue(fileRepository.existsById(file.getId()), "File should still exist: " + file.getName());
      } else {
        Assert.isTrue(!fileRepository.existsById(file.getId()), "Subdirectory should be deleted: " + file.getName());
      }
    }
  }
}