package moanote.backend.controller;

import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.dto.FileDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.service.FileService;
import moanote.backend.service.UserService;
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

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class FileControllerTest {

  @Autowired
  private UserService userService;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private FileService fileService;

  @Autowired
  private FileController fileController;

  @Test
  void listFiles() {
    UserData user = userService.createUser("testUser", "testPassword");
    UserData otherUser = userService.createUser("otherUser", "otherPassword");

    ArrayList<File> files = new ArrayList<>();
    File subdirectory = fileService.createFile(user.getId(), "subdirectory", FileType.DIRECTORY);
    File subSubdirectory = fileService.createFile(user.getId(), "subSubdirectory",
        FileType.DIRECTORY, subdirectory.getId());
    File rootDirectory = subdirectory.getDirectory();
    files.add(rootDirectory);
    files.add(subdirectory);
    files.add(subSubdirectory);
    files.add(fileService.createFile(user.getId(), "file1.txt", FileType.DOCUMENT));
    files.add(fileService.createFile(user.getId(), "file2.txt", FileType.DOCUMENT));
    files.add(fileService.createFile(user.getId(), "file3.txt", FileType.DOCUMENT));
    files.add(
        fileService.createFile(user.getId(), "file4.txt", FileType.DOCUMENT, subdirectory.getId()));
    files.add(
        fileService.createFile(user.getId(), "file5.txt", FileType.DOCUMENT, subdirectory.getId()));
    files.add(fileService.createFile(user.getId(), "file6.txt", FileType.DOCUMENT,
        subSubdirectory.getId()));

    File otherUserFile = fileService.createFile(otherUser.getId(), "otherUserFile.txt",
        FileType.DOCUMENT);

    {
      var response = fileController.listFiles(null, otherUser.getId(), false);
      Assert.isTrue(response.hasBody(), "body should not be null");
      if (response.getBody() != null) {
        Assert.isTrue(response.getBody().size() == 2, "Other user's files should not be listed");
        Assert.isTrue(response.getBody().contains(new FileDTO(otherUserFile)),
            "Other user's files should be listed " + otherUserFile.getName());
        Assert.isTrue(response.getBody().contains(new FileDTO(otherUserFile.getDirectory())),
            "Other user's files should be listed " + otherUserFile.getDirectory().getName());
      }
    }

    {
      var response = fileController.listFiles(null, user.getId(), false);
      if (response.getBody() == null) {
        fail("Response body should not be null");
      }
      for (File file : files) {
        if (!file.equals(rootDirectory) && !file.getDirectory().equals(rootDirectory)) {
          Assert.isTrue(!response.getBody().contains(new FileDTO(file)),
              "File should not be listed: " + file.getName());
          continue;
        }
        Assert.isTrue(response.getBody().contains(new FileDTO(file)),
            "File should be listed: " + file.getName());
      }
    }

    {
      var response = fileController.listFiles(null, user.getId(), true);
      if (response.getBody() == null) {
        fail("Response body should not be null");
      }
      for (File file : files) {
        Assert.isTrue(response.getBody().contains(new FileDTO(file)),
            "File should be listed: " + file.getName());
      }
    }

    {
      var response = fileController.listFiles(subdirectory.getId(), user.getId(), true);
      if (response.getBody() == null) {
        fail("Response body should not be null");
      }
      for (File file : files) {
        if (!file.equals(subdirectory) && (file.equals(rootDirectory) || file.getDirectory().equals(rootDirectory))) {
          Assert.isTrue(!response.getBody().contains(new FileDTO(file)),
              "File should not be listed: " + file.getName());
        } else {
          Assert.isTrue(response.getBody().contains(new FileDTO(file)),
              "File should be listed: " + file.getName());
        }
      }
    }

    {
      var response = fileController.listFiles(subSubdirectory.getId(), otherUser.getId(), true);
      Assert.isTrue(response.getStatusCode().is4xxClientError(),
          "Response should not be successful");
    }

    {
      var response = fileController.listFiles(subSubdirectory.getId(), otherUser.getId(), false);
      Assert.isTrue(response.getStatusCode().is4xxClientError(),
          "Response should not be successful");
    }
  }
}