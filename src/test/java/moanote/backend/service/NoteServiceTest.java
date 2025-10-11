package moanote.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.entity.*;
import moanote.backend.entity.File.FileType;
import moanote.backend.repository.DiagramNoteSegmentRepository;
import moanote.backend.repository.NoteRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
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


@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class NoteServiceTest {

  @Autowired
  private UserService userService;

  @Autowired
  private FileService fileService;

  @Autowired
  private NoteRepository noteRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private NoteService noteService;

  @Autowired
  private DiagramNoteSegmentRepository diagramRepository;

  @Autowired
  private TextNoteSegmentRepository textRepository;
  @Autowired
  private TextNoteSegmentRepository textNoteSegmentRepository;

  @Test
  void addNote() {
    UserData user = userService.createUser("tester", "tester");
    File file = fileService.createFile(user.getId(), "note", FileType.DOCUMENT);
    noteRepository.findNoteById(file.getNote().getId()).orElseThrow();
  }

  @Test
  void testSameIdFileAndNote() {
    UserData user = userService.createUser("tester", "tester");
    File file = fileService.createFile(user.getId(), "note", FileType.DOCUMENT);

    Note n1 = noteRepository.findNoteById(file.getNote().getId()).orElseThrow();
    Note n2 = noteRepository.findNoteById(file.getId()).orElseThrow();
    Assert.isTrue(n1.getId().equals(n2.getId()), "Note id and File id should be equal");
  }

  @Test
  @Transactional
  void deleteFileCascadeSegments() {
    UserData user = userService.createUser("tester", "tester");
    File file = fileService.createFile(user.getId(), "note", FileType.DOCUMENT);
    Note note = file.getNote();
    var sText = noteService.createTextNoteSegment(note.getId());
    var sDiagram = noteService.createDiagramNoteSegment(note.getId());

    entityManager.flush();
    var oText = textNoteSegmentRepository.findById(sText.getId());
    var oDiagram = diagramRepository.findById(sDiagram.getId());
    Assert.isTrue(oText.isPresent(), "text segment must be found");
    Assert.isTrue(oDiagram.isPresent(), "diagram segment must be found");

    entityManager.flush();
    fileService.deleteFile(file.getId(), user.getId());
    Assert.isTrue(textRepository.findById(sText.getId()).isEmpty(), "text segment should be removed");
    Assert.isTrue(diagramRepository.findById(sDiagram.getId()).isEmpty(), "diagram segment should be removed");
  }

  @Test
  @Transactional
  void createTextSegmentInitializesAndPersistsPlainText() {
    UserData user = userService.createUser("tester", "tester");
    File file = fileService.createFile(user.getId(), "note", FileType.DOCUMENT);
    Note note = file.getNote();
    TextNoteSegment segment = noteService.createTextNoteSegment(note.getId());
    textNoteSegmentRepository.saveAndFlush(segment);

    TextNoteSegment reloaded = textNoteSegmentRepository.findById(segment.getId()).orElseThrow();
    Assert.isTrue(reloaded.getContent().isEmpty(), "New text segments should start empty");

    reloaded.updateContent("hello");
    textNoteSegmentRepository.saveAndFlush(reloaded);

    TextNoteSegment updated = textNoteSegmentRepository.findById(segment.getId()).orElseThrow();
    Assert.isTrue("hello".equals(updated.getContent()), "Updated content must be persisted");
  }
}