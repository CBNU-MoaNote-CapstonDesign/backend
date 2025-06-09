package moanote.backend.repository;

import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.entity.File;
import moanote.backend.entity.UserData;
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

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class FileUserDataRepositoryTest {

  @Autowired
  private UserService userService;

  @Autowired
  private FileUserDataRepository fileUserDataRepository;

  @Autowired
  private FileService fileService;

  @Test
  void findOwnerByFile() {
    UserData user = userService.createUser("testUser", "testPassword");
    File file = fileService.createFile(user.getId(), "testFile.txt", File.FileType.DOCUMENT);
    Assert.isTrue(fileUserDataRepository.findOwnerByFile(file).getUser().equals(user), "Owner seems not found");
  }
}