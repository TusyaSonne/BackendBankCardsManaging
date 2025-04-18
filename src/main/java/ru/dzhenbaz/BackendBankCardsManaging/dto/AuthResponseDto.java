package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при успешной аутентификации (JWT токен)")
public class AuthResponseDto {

    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJVc2VyIGRldGFpbHMiLCJlbWFpbCI6ImF0dXIuZHpoZW5iYXpAZ21haWwuY29tIiwiaWF0IjoxNzQ0OTgxMTU0LCJpc3MiOiJEemhlbmJheiIsImV4cCI6MTc0NDk4NDc1NH0.FGVblpyeP6oxCANBzVlw7RtX8wQ45g8tq6Xtr4TvLhk")
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
