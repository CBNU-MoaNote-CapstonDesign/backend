package moanote.backend.service;

import moanote.backend.BackendApplication;
import moanote.backend.entity.Note;
import org.apache.hc.core5.util.Asserts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = BackendApplication.class)
class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @Test
    void createAndUpdateNote() {
        Note note = noteService.createNote();
        noteService.updateNote(note.getId(), "Hello, World!");
        Note createdNote = noteService.getNoteById(note.getId());
        Assert.isTrue(createdNote.getContent().equals("Hello, World!"), "Content is not updated\n");
    }
}