package moanote.backend.controller;

import moanote.backend.dto.AddSegmentDTO;
import moanote.backend.dto.NoteDTO;
import moanote.backend.dto.SegmentType;
import moanote.backend.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

  private final NoteService noteService;

  @Autowired
  public NoteController(NoteService noteService) {
    this.noteService = noteService;
  }

  @GetMapping("/metadata/{fileId}")
  public ResponseEntity<NoteDTO> getNoteMetadata(@PathVariable("fileId") UUID fileId,
      @RequestParam(name = "user") UUID userId) {
    try {
      return ResponseEntity.ok().body(noteService.getNoteMetadata(fileId, userId));
    } catch (NoSuchElementException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(404).build();
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(403).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @PostMapping("/{fileId}/add/segment")
  public ResponseEntity<UUID> addSegment(@PathVariable("fileId") UUID fileId,
      @RequestParam(name = "user") UUID userId, @RequestBody AddSegmentDTO addSegmentDTO) {
    try {
      return ResponseEntity.ok().body(noteService.createSegment(fileId, userId, addSegmentDTO));
    } catch (NoSuchElementException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(404).build();
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(403).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @PostMapping("/{fileId}/delete/segment")
  public ResponseEntity<Void> deleteSegment(@PathVariable("fileId") UUID fileId,
      @RequestParam(name = "user") UUID userId, @RequestParam(name = "segment") UUID segmentId) {
    System.out.println("Not implemented");
    return ResponseEntity.status(404).build();
  }
}