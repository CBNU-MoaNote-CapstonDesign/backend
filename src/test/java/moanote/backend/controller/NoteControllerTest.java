package moanote.backend.controller;

import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.UserData;
import moanote.backend.repository.NoteRepository;
import moanote.backend.service.FileService;
import moanote.backend.service.NoteService;
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


@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class NoteControllerTest {

  @Autowired
  private FileService fileService;

  @Autowired
  private NoteService noteService;

  @Autowired
  private NoteController noteController;

  @Autowired
  private UserService userService;

  @Autowired
  private NoteRepository noteRepository;

  @Test
  void addSegment() {
    UserData user = userService.createUser("tester", "tester");
    File file = fileService.createFile(user.getId(),"note", FileType.DOCUMENT);

    var textSegment = noteService.createTextNoteSegment(file.getNote().getId());
  }

  @Test
  void getNoteMetadata() {
    UserData user = userService.createUser("tester", "tester");
    File file = fileService.createFile(user.getId(),"note", FileType.DOCUMENT);

  }
}