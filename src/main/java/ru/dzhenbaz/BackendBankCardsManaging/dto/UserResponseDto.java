package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;

@Schema(description = "Информация о пользователе")
public class UserResponseDto {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Email пользователя", example = "admin@gmail.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "ROLE_ADMIN")
    private Role role;

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
