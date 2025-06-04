package moanote.backend.controller;


import moanote.backend.dto.FileCreateDTO;
import moanote.backend.dto.FileDTO;
import moanote.backend.dto.FileEditDTO;
import moanote.backend.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

  final private FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping({"/list", "/list/{fileId}"})
  public ResponseEntity<List<FileDTO>> listFiles(@PathVariable(required = false) UUID fileId, @RequestParam(name = "user", required = true) UUID userId, @RequestParam(name = "recursive", defaultValue = "false") boolean recursive) {

    try {
      if (fileId != null && !fileService.hasAnyPermission(fileId, userId)) {
        return ResponseEntity.status(403).body(List.of());
      }
      return ResponseEntity.ok().body(fileService.getFilesByDirectory(fileId, recursive));
    } catch (Exception e) {
      return ResponseEntity.status(404).body(List.of());
    }
  }

  @PostMapping("/create/{directoryId}")
  public ResponseEntity<FileDTO> createFile(@PathVariable(required = false) UUID directoryId, @RequestParam(name = "user") UUID userId, @RequestBody FileCreateDTO fileCreateDTO) {

    FileDTO createdFile;
    try {
      if (directoryId != null) {
        if (!fileService.hasAnyPermission(directoryId, userId)) {
          return ResponseEntity.status(403).body(null);
        }
        createdFile = new FileDTO(fileService.createFile(userId, fileCreateDTO.name(), fileCreateDTO.type(), directoryId));
      } else {
        createdFile = new FileDTO(fileService.createFile(userId, fileCreateDTO.name(), fileCreateDTO.type()));
      }
      return ResponseEntity.status(201).body(createdFile);
    } catch (Exception e) {
      return ResponseEntity.status(400).body(null);
    }
  }

  @PostMapping("/edit/{fileId}")
  public ResponseEntity<FileDTO> editFile(@PathVariable UUID fileId, @RequestParam(name = "user") UUID userId, @RequestBody FileEditDTO fileEditDTO) {
    try {
      return ResponseEntity.ok().body(new FileDTO(fileService.editFile(userId, fileId, fileEditDTO)));
    } catch (NoSuchElementException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(404).body(null);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(403).body(null);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(500).body(null);
    }
  }
}
