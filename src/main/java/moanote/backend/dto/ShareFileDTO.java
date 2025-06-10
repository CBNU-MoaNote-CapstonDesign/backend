package moanote.backend.dto;

import moanote.backend.entity.FileUserData.Permission;

public record ShareFileDTO(String username, Permission permission) {

}
