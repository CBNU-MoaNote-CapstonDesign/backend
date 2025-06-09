package moanote.backend.controller;

import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.dto.FileDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.FileUserData;
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
        Assert.isTrue(response.getBody().contains(new FileDTO(otherUserFile, otherUser)),
            "Other user's files should be listed " + otherUserFile.getName());
        Assert.isTrue(response.getBody().contains(new FileDTO(otherUserFile.getDirectory(), otherUser)),
            "Other user's files should be listed " + otherUserFile.getDirectory().getName());
      }
    }

    {
      var response = fileController.listFiles(null, user.getId(), false);
      if (response.getBody() == null) {
        fail("Response body should not be null");
      }
      for (File file : files) {
        if (!file.equals(rootDirectory) && !file.getDirectory().getId().equals(rootDirectory.getId())) {
          Assert.isTrue(!response.getBody().contains(new FileDTO(file, user)),
              "File should not be listed: " + file.getName());
          continue;
        }
        Assert.isTrue(response.getBody().contains(new FileDTO(file, user)),
            "File should be listed: " + file.getName());
      }
    }

    {
      var response = fileController.listFiles(null, user.getId(), true);
      if (response.getBody() == null) {
        fail("Response body should not be null");
      }
      for (File file : files) {
        Assert.isTrue(response.getBody().contains(new FileDTO(file, user)),
            "File should be listed: " + file.getName());
      }
    }

    {
      var response = fileController.listFiles(subdirectory.getId(), user.getId(), true);
      if (response.getBody() == null) {
        fail("Response body should not be null");
      }
      for (File file : files) {
        if (!file.getId().equals(subdirectory.getId()) && (file.getId().equals(rootDirectory.getId()) || file.getDirectory().getId().equals(rootDirectory.getId()))) {
          Assert.isTrue(!response.getBody().contains(new FileDTO(file, user)),
              "File should not be listed: " + file.getName());
        } else {
          Assert.isTrue(response.getBody().contains(new FileDTO(file, user)),
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

  @Test
  @Transactional
  void fileMetadata() {
    UserData user = userService.createUser("testUser", "testPassword");
    UserData otherUser = userService.createUser("otherUser", "otherPassword");

    File file = fileService.createFile(user.getId(), "file", FileType.DOCUMENT);

    {
      var response = fileController.fileMetadata(file.getId(), user.getId());
      Assert.isTrue(response.getStatusCode().is2xxSuccessful(),
          "Response should be successful for owner");
      Assert.isTrue(response.getBody() != null && response.getBody().equals(new FileDTO(file, user)),
          "Response body should match the file");
    }

    {
      var response = fileController.fileMetadata(file.getId(), otherUser.getId());
      Assert.isTrue(response.getStatusCode().is4xxClientError(),
          "Response should not be successful for non-owner");
    }
  }

  @Test
  void deleteFile() {
    UserData user = userService.createUser("testUser", "testPassword");
    UserData otherUser = userService.createUser("otherUser", "otherPassword");

    File file = fileService.createFile(user.getId(), "file", FileType.DOCUMENT);
    fileService.grantPermission(file.getId(), otherUser.getId(), FileUserData.Permission.WRITE);

    {
      var response = fileController.deleteFile(file.getId(), user.getId());
      Assert.isTrue(response.getStatusCode().is2xxSuccessful(),
          "Response should be successful for owner");
      Assert.isTrue(fileRepository.findById(file.getId()).isEmpty(),
          "File should be deleted");
    }

    {
      var response = fileController.deleteFile(file.getId(), user.getId());
      Assert.isTrue(response.getStatusCode().is4xxClientError(),
          "Response should not be successful for deleted file");
    }

    {
      var files = fileService.getFilesByOwnerUserId(user.getId());
      Assert.isTrue(files.size() == 1, "No files should not be left for the user but " + (files.size() - 1) + " left (except for root)");
    }

    {
      var files = fileService.getFilesByUserId(otherUser.getId());
      Assert.isTrue(files.size() == 1, "No files should not be left for the user");
    }
  }
}