package moanote.backend.dto;

import moanote.backend.entity.File.FileType;

public record FileCreateDTO(String name, FileType type) {

}
