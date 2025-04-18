package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LimitResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LimitUpdateRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Limit;
import ru.dzhenbaz.BackendBankCardsManaging.service.LimitService;

import java.util.List;

@RestController
@RequestMapping("/limits")
@Tag(name = "3. Лимиты", description = "Управление дневным лимитом на операции по картам")
public class LimitController {

    private final LimitService limitService;

    @Autowired
    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }

    @Operation(summary = "Получить текущий дневной лимит")
    @GetMapping("/daily")
    public ResponseEntity<LimitResponseDto> getDailyLimit() {
        return ResponseEntity.ok(limitService.getDailyLimit());
    }

    @Operation(summary = "Изменить дневной лимит (доступно только администратору)")
    @PostMapping("/daily")
    ResponseEntity<LimitResponseDto> changeDailyLimit(@RequestBody @Valid LimitUpdateRequestDto request) {
        return ResponseEntity.ok(limitService.updateDailyLimit(request.getNewValue()));
    }
}
