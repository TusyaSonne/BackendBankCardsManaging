package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.AuthResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.RegisterRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.service.AuthService;
import ru.dzhenbaz.BackendBankCardsManaging.util.OnLogin;
import ru.dzhenbaz.BackendBankCardsManaging.util.OnRegister;

@RestController
@RequestMapping("/auth")
@Tag(name = "1. Аутентификация", description = "Регистрация и вход в систему")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Validated(OnRegister.class) RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Вход пользователя в систему")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Validated(OnLogin.class) RegisterRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
