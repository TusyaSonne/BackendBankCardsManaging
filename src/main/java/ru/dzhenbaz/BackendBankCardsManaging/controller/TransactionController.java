package ru.dzhenbaz.BackendBankCardsManaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dzhenbaz.BackendBankCardsManaging.dto.*;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;
import ru.dzhenbaz.BackendBankCardsManaging.service.AuthService;
import ru.dzhenbaz.BackendBankCardsManaging.service.TransactionService;

/**
 * Контроллер для управления транзакциями по картам.
 * Позволяет получать историю транзакций, снимать средства и переводить деньги между своими картами.
 */
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

    /**
     * Получает список всех транзакций текущего пользователя или всех транзакций (если администратор).
     *
     * @param type фильтрация по типу транзакции (опционально)
     * @param page номер страницы
     * @param size размер страницы
     * @return страница транзакций
     */
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

    /**
     * Получает транзакции, относящиеся к конкретной карте.
     *
     * @param cardId идентификатор карты
     * @param type   фильтрация по типу транзакции (опционально)
     * @param page   номер страницы
     * @param size   размер страницы
     * @return страница транзакций по карте
     */
    @Operation(summary = "Получить транзакции по конкретной карте (пользователь - только по своей)")
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

    /**
     * Выполняет снятие средств с указанной карты.
     *
     * @param request данные о снятии
     * @return информация о выполненной транзакции
     */
    @Operation(summary = "Снять средства со своей карты")
    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponseDto> withdraw(@RequestBody @Valid WithdrawRequestDto request) {
        WithdrawResponseDto transaction = transactionService.withdraw(
                request.getCardId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(transaction);
    }

    /**
     * Выполняет перевод средств между двумя своими картами.
     *
     * @param request данные о переводе
     * @return информация о выполненном переводе
     */
    @Operation(summary = "Перевод средств между своими картами")
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
