package moanote.backend.controller;


import moanote.backend.dto.FileCreateDTO;
import moanote.backend.dto.FileDTO;
import moanote.backend.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

  final private FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/list/{fileId}")
  public ResponseEntity<List<FileDTO>> listFiles(@PathVariable(required = false) UUID fileId, @RequestParam(name = "user", required = true) UUID userId, @RequestParam(name = "recursive", defaultValue = "false") boolean recursive) {

    try {
      if (!fileService.hasAnyPermission(fileId, userId)) {
        return ResponseEntity.status(403).body(List.of());
      }
      return ResponseEntity.ok().body(fileService.getFilesByDirectory(fileId, recursive));
    } catch (Exception e) {
      return ResponseEntity.status(404).body(List.of());
    }
  }

  @PostMapping("/api/files/create/{directoryId}")
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
}
