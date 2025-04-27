package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LimitResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LimitUpdateRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.service.LimitService;

/**
 * Контроллер для управления дневными лимитами операций по картам.
 * Позволяет получать текущий лимит и изменять его.
 */
@RestController
@RequestMapping("/limits")
@Tag(name = "3. Лимиты", description = "Управление дневным лимитом на операции по картам")
public class LimitController {

    private final LimitService limitService;

    @Autowired
    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }

    /**
     * Получает текущий дневной лимит на снятие средств.
     *
     * @return информация о лимите
     */
    @Operation(summary = "Получить текущий дневной лимит")
    @GetMapping("/daily")
    public ResponseEntity<LimitResponseDto> getDailyLimit() {
        return ResponseEntity.ok(limitService.getDailyLimit());
    }

    /**
     * Изменяет значение дневного лимита.
     * Доступно только администраторам.
     *
     * @param request запрос с новым значением лимита
     * @return обновленная информация о лимите
     */
    @Operation(summary = "Изменить дневной лимит (доступно только администратору)")
    @PostMapping("/daily")
    ResponseEntity<LimitResponseDto> changeDailyLimit(@RequestBody @Valid LimitUpdateRequestDto request) {
        return ResponseEntity.ok(limitService.updateDailyLimit(request.getNewValue()));
    }
}
