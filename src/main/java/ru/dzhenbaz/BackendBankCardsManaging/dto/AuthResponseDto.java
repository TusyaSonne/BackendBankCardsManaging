package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при успешной аутентификации (JWT токен)")
public class AuthResponseDto {

    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...signature")
    private String token;

    public AuthResponseDto(String token) {
        this.token = token;
    }

    public AuthResponseDto() {
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
