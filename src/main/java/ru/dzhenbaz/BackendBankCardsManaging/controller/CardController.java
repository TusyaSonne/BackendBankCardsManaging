package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
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

/**
 * Контроллер для управления банковскими картами.
 * Позволяет создавать, получать, удалять карты и изменять их статусы.
 */
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

    /**
     * Создает новую карту для указанного пользователя (только администратор).
     *
     * @param request данные для создания карты
     * @return созданная карта
     */
    @Operation(summary = "Создать новую карту (только администратор)")
    @PostMapping()
    public ResponseEntity<CardResponseDto> create(@RequestBody @Valid CardCreateRequestDto request) {
        return ResponseEntity.ok(cardService.createCard(request.getUserId(), request.getBalance()));
    }

    /**
     * Получает список карт пользователя или все карты (если администратор).
     *
     * @param status фильтрация по статусу карты (опционально)
     * @param page   номер страницы
     * @param size   размер страницы
     * @return страница карт
     */
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


    /**
     * Получает карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return карта или сообщение об ошибке доступа
     */
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

    /**
     * Удаляет карту по её идентификатору (только для администратора).
     *
     * @param id идентификатор карты
     * @return HTTP 204 No Content
     */
    @Operation(summary = "Удалить карту (доступно только администратору)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Изменяет статус карты.
     * Администратор может установить любой статус, пользователь может только заблокировать свою карту.
     *
     * @param id      идентификатор карты
     * @param request запрос с новым статусом
     * @return обновленная карта или сообщение об ошибке
     */
    @Operation(summary = "Изменить статус карты",
            description = "Изменение статуса карты на любой из доступных (ACTIVE, BLOCKED, EXPIRED)" +
                    " для Администратора, запрос на блокировку карты для пользователя " +
                    "(доступен только статус BLOCKED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateCardStatus(@PathVariable Long id,
                                              @RequestBody @Valid CardStatusUpdateRequestDto request) {
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
