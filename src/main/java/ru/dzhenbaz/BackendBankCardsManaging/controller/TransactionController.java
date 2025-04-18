package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.*;
import ru.dzhenbaz.BackendBankCardsManaging.model.Transaction;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;
import ru.dzhenbaz.BackendBankCardsManaging.service.AuthService;
import ru.dzhenbaz.BackendBankCardsManaging.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "4. Транзакции", description = "Просмотр и выполнение транзакций по картам")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    @Autowired
    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @Operation(summary = "Получить список всех транзакций (пользователь — только свои)")
    @GetMapping
    public ResponseEntity<Page<TransactionResponseDto>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getAllTransactions(user, type, pageable));
    }

    @Operation(summary = "Получить транзакции по конкретной карте")
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<Page<TransactionResponseDto>> getByCard(
            @PathVariable Long cardId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getByCardId(cardId, type, pageable));
    }

    @Operation(summary = "Снять средства с карты")
    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponseDto> withdraw(@RequestBody @Valid WithdrawRequestDto request) {
        WithdrawResponseDto transaction = transactionService.withdraw(
                request.getCardId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Перевести средства между своими картами")
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(@RequestBody @Valid TransferRequestDto request) {
        TransferResponseDto response = transactionService.transfer(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(response);
    }


}
