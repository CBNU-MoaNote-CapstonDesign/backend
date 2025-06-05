package moanote.backend.repository;

import com.github.f4b6a3.uuid.util.UuidComparator;
import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class FileRepositoryTest {

  @Autowired
  private UserService userService;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private FileService fileService;


  @Test
  void findFilesByOwnerTest() {
    UserData user = userService.createUser("testUser", "testPassword");
    UserData otherUser = userService.createUser("otherUser", "otherPassword");

    ArrayList<File> files = new ArrayList<>();
    files.add(fileService.createRootDirectory(user.getId()));
    files.add(fileService.createFile(user.getId(), "testFile1.txt", FileType.DOCUMENT));
    files.add(fileService.createFile(user.getId(), "testFile2.txt", FileType.DOCUMENT));
    files.add(fileService.createFile(user.getId(), "testFile3.txt", FileType.DOCUMENT));

    fileService.createRootDirectory(otherUser.getId());
    fileService.createFile(otherUser.getId(), "otherFile.txt", FileType.DOCUMENT);
    fileService.createFile(otherUser.getId(), "otherDirectory", FileType.DIRECTORY);

    List<File> foundNotes = fileRepository.findFilesByOwner(user).stream()
        .sorted((o1, o2) -> UuidComparator.defaultCompare(o1.getId(), o2.getId())).toList();

    assertEquals(4, foundNotes.size());
    files.sort((o1, o2) -> UuidComparator.defaultCompare(o1.getId(), o2.getId()));
    assertEquals(files.get(0).getId(), foundNotes.get(0).getId());
    assertEquals(files.get(1).getId(), foundNotes.get(1).getId());
    assertEquals(files.get(2).getId(), foundNotes.get(2).getId());
    assertEquals(files.get(3).getId(), foundNotes.get(3).getId());
  }
}