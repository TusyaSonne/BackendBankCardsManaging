package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * Содержит email, пароль и роль.
 */
@Schema(description = "Запрос для регистрации пользователя")
public class RegisterRequestDto {

    @Schema(description = "Email пользователя", example = "user@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Пароль", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    @Schema(description = "Роль пользователя (ROLE_USER или ROLE_ADMIN)", example = "ROLE_USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    private Role role;

    public RegisterRequestDto() {
    }

    public RegisterRequestDto(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
