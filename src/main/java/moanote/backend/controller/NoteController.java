package moanote.backend.controller;

import moanote.backend.dto.AddSegmentDTO;
import moanote.backend.dto.NoteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

  @GetMapping("/metadata/{fileId}")
  public ResponseEntity<NoteDTO> getNoteMetadata(@PathVariable("fileId") UUID fileId,
      @RequestParam(name = "user") UUID userId) {
    return ResponseEntity.status(404).build();
    // return ResponseEntity.ok().body(null);
  }

  @PostMapping("/{fileId}/add/segment")
  public ResponseEntity<NoteDTO> addSegment(@PathVariable("fileId") UUID fileId,
      @RequestParam(name = "user") UUID userId, @RequestBody AddSegmentDTO addSegmentDTO) {
    return ResponseEntity.status(404).build();
    // return ResponseEntity.ok().body(null);
  }

  @PostMapping("/{fileId}/delete/segment")
  public ResponseEntity<Void> deleteSegment(@PathVariable("fileId") UUID fileId,
      @RequestParam(name = "user") UUID userId, @RequestParam(name = "segment") UUID segmentId) {
    return ResponseEntity.status(404).build();
    // return ResponseEntity.status(204).build();
  }
}