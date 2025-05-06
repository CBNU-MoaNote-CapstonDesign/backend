package moanote.backend.dto;

public record UserRegisterDTO(String username, String password) {
    public UserRegisterDTO {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }

}
