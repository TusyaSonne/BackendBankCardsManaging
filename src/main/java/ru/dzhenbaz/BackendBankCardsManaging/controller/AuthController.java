package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.AuthResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LoginRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.RegisterRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.service.AuthService;

/**
 * Контроллер для операций аутентификации.
 * Позволяет регистрировать новых пользователей и входить в систему.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "1. Аутентификация", description = "Регистрация и вход в систему")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * Регистрирует нового пользователя.
     *
     * @param request данные для регистрации
     * @return токен для дальнейшей аутентификации
     */
    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param request данные для входа
     * @return токен для дальнейшей аутентификации
     */
    @Operation(summary = "Вход пользователя в систему")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
