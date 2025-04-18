package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;

@Schema(description = "Запрос на изменение роли пользователя")
public class ChangeRoleRequestDto {

    @Schema(description = "Новая роль пользователя", example = "ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is required")
    private Role newRole;

    public ChangeRoleRequestDto() {
    }

    public ChangeRoleRequestDto(Role newRole) {
        this.newRole = newRole;
    }

    public Role getNewRole() {
        return newRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }
}
