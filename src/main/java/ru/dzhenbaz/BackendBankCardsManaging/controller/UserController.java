package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.ChangeRoleRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.UserResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.service.AuthService;
import ru.dzhenbaz.BackendBankCardsManaging.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "5. Пользователи", description = "Операции управления пользователями (только для Администратора)")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserService userService, AuthService authService, ModelMapper modelMapper) {
        this.userService = userService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers()
                .stream().map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList());
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getById(id)
                .map(user -> ResponseEntity.ok(modelMapper.map(user, UserResponseDto.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Изменить роль пользователя")
    @PostMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody @Valid ChangeRoleRequestDto request) {

        User currentUser = authService.getCurrentUser();

        try {
            userService.changeUserRole(id, request.getNewRole(), currentUser);
            return ResponseEntity.ok("User role updated to " + request.getNewRole());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}

