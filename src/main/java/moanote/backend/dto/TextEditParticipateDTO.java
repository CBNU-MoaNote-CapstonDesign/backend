package moanote.backend.dto;

import java.util.List;

public record TextEditParticipateDTO(NoteDTO note, List<TextSegmentDTO> textNoteSegments) {

}
