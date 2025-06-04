package moanote.backend.service;

import jakarta.transaction.Transactional;
import moanote.backend.BackendApplication;
import moanote.backend.entity.File;
import moanote.backend.entity.File.FileType;
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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.properties")
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BackendApplication.class)
class FileServiceTest {

  @Autowired
  private FileService fileService;

  @Autowired
  private FileRepository fileRepository;

  @Test
  void removeFilesRecursively() {
    File rootDirectory = fileRepository.createRootDirectory();
    File subDirectory;
    ArrayList<File> subFilesAtRoot = new ArrayList<>();
    ArrayList<File> subFilesAtSubDirectory = new ArrayList<>();

    subFilesAtRoot.add(fileRepository.createFile("Doxyfile", FileType.DOCUMENT, rootDirectory));
    subFilesAtRoot.add(fileRepository.createFile("Makefile", FileType.DOCUMENT, rootDirectory));
    subFilesAtRoot.add(fileRepository.createFile("Doxyfile", FileType.DOCUMENT, rootDirectory));
    subDirectory = fileRepository.createFile("src", FileType.DIRECTORY, rootDirectory);
    subFilesAtRoot.add(subDirectory);
    subFilesAtSubDirectory.add(fileRepository.createFile("main.cpp", FileType.DOCUMENT, subDirectory));
    subFilesAtSubDirectory.add(fileRepository.createFile("service.cpp", FileType.DOCUMENT, subDirectory));
    subFilesAtSubDirectory.add(fileRepository.createFile("service.hpp", FileType.DOCUMENT, subDirectory));
    subFilesAtSubDirectory.add(fileRepository.createFile("service.cpp", FileType.DOCUMENT, subDirectory));

    subFilesAtSubDirectory.add(fileRepository.createFile("dir", FileType.DIRECTORY, subDirectory));
    File subSubDirectory = subFilesAtSubDirectory.getLast();
    subFilesAtSubDirectory.add(fileRepository.createFile("file.txt", FileType.DOCUMENT, subSubDirectory));

    fileService.removeFilesRecursively(subDirectory);
    for (File file : subFilesAtSubDirectory) {
      assertFalse(fileRepository.existsById(file.getId()), "File should be deleted: " + file.getName());
    }
    for (File file : subFilesAtRoot) {
      if (!file.equals(subDirectory)) {
        assertTrue(fileRepository.existsById(file.getId()), "File should still exist: " + file.getName());
      } else {
        assertFalse(fileRepository.existsById(file.getId()), "Subdirectory should be deleted: " + file.getName());
      }
    }

    fileService.removeFilesRecursively(rootDirectory);
    for (File file : subFilesAtRoot) {
      assertFalse(fileRepository.existsById(file.getId()), "Root directory and its files should be deleted: " + file.getName());
    }
  }
}