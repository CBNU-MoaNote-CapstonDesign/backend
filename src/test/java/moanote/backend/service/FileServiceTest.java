package moanote.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.dto.FileCreateDTO;
import moanote.backend.dto.FileDTO;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
import moanote.backend.entity.Note;
import moanote.backend.entity.UserData;
import moanote.backend.repository.FileRepository;
import moanote.backend.repository.TextNoteSegmentRepository;
import moanote.backend.repository.DiagramNoteSegmentRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
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

  @Autowired
  private NoteService noteService;

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

  @Test
  @Transactional
  void createFile_isCodeTrue() {
    UserData userData = userService.createUser("testuser2", "testpassword2");
    File rootDirectory = fileRepository.getRootDirectory(userData);

    FileCreateDTO createRequest = new FileCreateDTO("file.js", FileType.DOCUMENT, true, Note.CodeLanguage.JAVASCRIPT);

    FileDTO fileDTO = fileService.createFile(rootDirectory.getId(), userData.getId(), createRequest);

    File file = fileRepository.getReferenceById(fileDTO.id());
    Note note = file.getNote();
    Assert.isTrue(note != null, "Note should not be null");
    Assert.isTrue(file.getNote().getType().equals(Note.NoteType.CODE), "File should be of type CODE");
    Assert.isTrue(file.getNote().getCodeLanguage().equals(Note.CodeLanguage.JAVASCRIPT), "File should be of language JAVASCRIPT");
  }

  @Test
  @Transactional
  void deleteNestedDirectory() {
    UserData user = userService.createUser("delete-test-user", "delete-test-password");
    File rootDirectory = fileRepository.getRootDirectory(user);

    File projectDirectory = fileService.createFile(user.getId(), "project", FileType.DIRECTORY, rootDirectory.getId());
    File apiDirectory = fileService.createFile(user.getId(), "api", FileType.DIRECTORY, projectDirectory.getId());
    File publicDirectory = fileService.createFile(user.getId(), "public", FileType.DIRECTORY, projectDirectory.getId());
    File handlersDirectory = fileService.createFile(user.getId(), "handlers", FileType.DIRECTORY, apiDirectory.getId());
    File utilsDirectory = fileService.createFile(user.getId(), "utils", FileType.DIRECTORY, handlersDirectory.getId());
    File componentsDirectory = fileService.createFile(user.getId(), "components", FileType.DIRECTORY, publicDirectory.getId());
    File layoutDirectory = fileService.createFile(user.getId(), "layout", FileType.DIRECTORY, componentsDirectory.getId());

    File packageJson = fileService.createFile(user.getId(), "package.json", FileType.DOCUMENT, projectDirectory.getId());
    File packageLock = fileService.createFile(user.getId(), "package-lock.json", FileType.DOCUMENT, projectDirectory.getId());
    File serverJs = fileService.createFile(user.getId(), "server.js", FileType.DOCUMENT, projectDirectory.getId());

    File problemJs = fileService.createFile(user.getId(), "problem.js", FileType.DOCUMENT, apiDirectory.getId());
    File randomProblemsJs = fileService.createFile(user.getId(), "random-problems.js", FileType.DOCUMENT, apiDirectory.getId());
    File routeHandlerJs = fileService.createFile(user.getId(), "route-handler.js", FileType.DOCUMENT, handlersDirectory.getId());
    File authMiddlewareJs = fileService.createFile(user.getId(), "auth-middleware.js", FileType.DOCUMENT, utilsDirectory.getId());

    File indexHtml = fileService.createFile(user.getId(), "index.html", FileType.DOCUMENT, publicDirectory.getId());
    File scriptJs = fileService.createFile(user.getId(), "script.js", FileType.DOCUMENT, publicDirectory.getId());
    File styleCss = fileService.createFile(user.getId(), "style.css", FileType.DOCUMENT, publicDirectory.getId());
    File navbarJs = fileService.createFile(user.getId(), "navbar.js", FileType.DOCUMENT, componentsDirectory.getId());
    File footerJs = fileService.createFile(user.getId(), "footer.js", FileType.DOCUMENT, componentsDirectory.getId());
    File mainLayoutJs = fileService.createFile(user.getId(), "main-layout.js", FileType.DOCUMENT, layoutDirectory.getId());

    noteService.createTextNoteSegment(packageJson.getNote().getId());
    noteService.createTextNoteSegment(packageLock.getNote().getId());
    noteService.createTextNoteSegment(serverJs.getNote().getId());
    noteService.createTextNoteSegment(serverJs.getNote().getId());

    noteService.createTextNoteSegment(problemJs.getNote().getId());
    noteService.createTextNoteSegment(randomProblemsJs.getNote().getId());
    noteService.createTextNoteSegment(routeHandlerJs.getNote().getId());
    noteService.createTextNoteSegment(authMiddlewareJs.getNote().getId());
    noteService.createTextNoteSegment(authMiddlewareJs.getNote().getId());

    noteService.createTextNoteSegment(indexHtml.getNote().getId());
    noteService.createTextNoteSegment(scriptJs.getNote().getId());
    noteService.createTextNoteSegment(styleCss.getNote().getId());
    noteService.createTextNoteSegment(navbarJs.getNote().getId());
    noteService.createTextNoteSegment(footerJs.getNote().getId());
    noteService.createTextNoteSegment(mainLayoutJs.getNote().getId());
    noteService.createTextNoteSegment(mainLayoutJs.getNote().getId());

    entityManager.flush();
    entityManager.clear();

    Assertions.assertDoesNotThrow(() -> {
      fileService.deleteFile(projectDirectory.getId(), user.getId());
      entityManager.flush();
    });
  }
}