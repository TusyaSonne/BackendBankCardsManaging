package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.util.OnLogin;
import ru.dzhenbaz.BackendBankCardsManaging.util.OnRegister;

@Schema(description = "Запрос для регистрации или входа пользователя")
public class RegisterRequestDto {

    @Schema(description = "Email пользователя", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email must not be blank", groups = {OnRegister.class, OnLogin.class})
    @Email(message = "Email must be valid", groups = {OnRegister.class, OnLogin.class})
    private String email;

    @Schema(description = "Пароль", example = "mySecret123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password must not be blank", groups = {OnRegister.class, OnLogin.class})
    @Size(min = 5, message = "Password must be at least 5 characters", groups = OnRegister.class)
    private String password;

    @Schema(description = "Роль пользователя (ROLE_USER или ROLE_ADMIN)", example = "ROLE_USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is required", groups = OnRegister.class)
    @Enumerated(EnumType.STRING)
    private Role role;

    public RegisterRequestDto() {}

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
