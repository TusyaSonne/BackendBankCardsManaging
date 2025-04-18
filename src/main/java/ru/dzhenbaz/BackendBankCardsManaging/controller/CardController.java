package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.CardCreateRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.CardResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.CardStatusUpdateRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;
import ru.dzhenbaz.BackendBankCardsManaging.service.AuthService;
import ru.dzhenbaz.BackendBankCardsManaging.service.CardService;

import java.util.List;

@RestController
@RequestMapping("/cards")
@Tag(name = "2. Банковские карты", description = "Операции с банковскими картами")
public class CardController {

    private final CardService cardService;
    private final AuthService authService;

    @Autowired
    public CardController(CardService cardService, AuthService authService, ModelMapper modelMapper) {
        this.cardService = cardService;
        this.authService = authService;
    }

    @Operation(summary = "Создать новую карту (только администратор)")
    @PostMapping()
    public ResponseEntity<CardResponseDto> create(@RequestBody @Valid CardCreateRequestDto request) {
        return ResponseEntity.ok(cardService.createCard(request.getUserId(), request.getBalance()));
    }

    @Operation(summary = "Получить список карт (пользователь видит только свои)")
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getAll(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User currentUser = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getAllCards(currentUser, status, pageable));
    }


    @Operation(summary = "Получить карту по ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        User currentUser = authService.getCurrentUser();

        try {
            return cardService.getById(id, currentUser)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).body(ex.getMessage());
        }
    }

    @Operation(summary = "Удалить карту (доступно только администратору)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Изменить статус карты",
            description = "Изменение статуса карты на любой из доступных (ACTIVE, BLOCKED, EXPIRED) для Администратора, " +
                    "запрос на блокировку карты для пользователя (доступен только статус BLOCKED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateCardStatus(@PathVariable Long id, @RequestBody @Valid CardStatusUpdateRequestDto request) {
        User user = authService.getCurrentUser();

        try {
            return cardService.changeCardStatus(id, request.getNewStatus(), user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
