package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.dto.TransactionResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.TransferResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.WithdrawResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.Limit;
import ru.dzhenbaz.BackendBankCardsManaging.model.Transaction;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;
import ru.dzhenbaz.BackendBankCardsManaging.repository.CardRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.LimitRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сервис для управления транзакциями.
 * Реализует операции снятия средств, перевода между картами, а также получение списка транзакций.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AuthService authService;
    private final LimitRepository limitRepository;
    private final ModelMapper modelMapper;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param transactionRepository репозиторий транзакций
     * @param cardRepository        репозиторий карт
     * @param authService           сервис аутентификации
     * @param limitRepository       репозиторий лимитов
     * @param modelMapper           маппер для преобразования сущностей в DTO
     */
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, CardRepository cardRepository,
                              AuthService authService, LimitRepository limitRepository, ModelMapper modelMapper) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.authService = authService;
        this.limitRepository = limitRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Получает список транзакций по карте с возможной фильтрацией по типу операции.
     *
     * @param cardId   идентификатор карты
     * @param type     тип транзакции (опционально)
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getByCardId(Long cardId, TransactionType type, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getOwner().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        Page<Transaction> txPage = (type != null)
                ? transactionRepository.findAllByCardAndType(card, type, pageable)
                : transactionRepository.findAllByCard(card, pageable);

        return txPage.map(this::mapToDto);
    }

    /**
     * Получает список всех транзакций для текущего пользователя.
     *
     * @param currentUser текущий пользователь
     * @param type        тип транзакции (опционально)
     * @param pageable    параметры пагинации
     * @return страница транзакций
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getAllTransactions(User currentUser,
                                                           TransactionType type, Pageable pageable) {
        Page<Transaction> txPage;

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            txPage = (type != null)
                    ? transactionRepository.findAllByType(type, pageable)
                    : transactionRepository.findAll(pageable);

        } else {
            if (type != null) {
                txPage = transactionRepository.findAllByCard_OwnerAndType(currentUser, type, pageable);
            } else {
                txPage = transactionRepository.findAllByCard_Owner(currentUser, pageable);
            }
        }

        return txPage.map(this::mapToDto);
    }

    /**
     * Выполняет операцию снятия средств с карты.
     *
     * @param cardId      идентификатор карты
     * @param amount      сумма снятия
     * @param description описание операции (опционально)
     * @return DTO с деталями операции
     */
    @Transactional
    public WithdrawResponseDto withdraw(Long cardId, BigDecimal amount, String description) {
        User user = authService.getCurrentUser();
        Card card = getCardForCurrentUser(cardId, user);

        validateCard(card);
        validateLimit(card, amount);

        if (card.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);

        Transaction tx = new Transaction();
        tx.setCard(card);
        tx.setAmount(amount);
        tx.setType(TransactionType.WITHDRAW);
        tx.setTimestamp(LocalDateTime.now());
        tx.setDescription(description);
        transactionRepository.save(tx);

        WithdrawResponseDto dto = modelMapper.map(tx, WithdrawResponseDto.class);
        dto.setCardNumber(tx.getCard().getCardNumber());

        return dto;
    }

    /**
     * Выполняет перевод средств между двумя картами одного пользователя.
     *
     * @param fromCardId  идентификатор карты отправителя
     * @param toCardId    идентификатор карты получателя
     * @param amount      сумма перевода
     * @param description описание операции (опционально)
     * @return DTO с деталями перевода
     */
    @Transactional
    public TransferResponseDto transfer(Long fromCardId, Long toCardId, BigDecimal amount, String description) {
        User user = authService.getCurrentUser();

        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        Card fromCard = getCardForCurrentUser(fromCardId, user);
        Card toCard = getCardForCurrentUser(toCardId, user);

        validateCard(fromCard);
        validateCard(toCard);

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction txFrom = new Transaction();
        txFrom.setCard(fromCard);
        txFrom.setAmount(amount);
        txFrom.setType(TransactionType.TRANSFER);
        txFrom.setTimestamp(LocalDateTime.now());
        txFrom.setDescription("Transfer to card #" + toCard.getCardNumber() + " — " + description);
        transactionRepository.save(txFrom);

        Transaction txTo = new Transaction();
        txTo.setCard(toCard);
        txTo.setAmount(amount);
        txTo.setType(TransactionType.TRANSFER);
        txTo.setTimestamp(LocalDateTime.now());
        txTo.setDescription("Received from card #" + fromCard.getCardNumber() + " — " + description);
        transactionRepository.save(txTo);

        return new TransferResponseDto(
                "Transfer completed successfully",
                maskCardNumber(fromCard.getCardNumber()),
                maskCardNumber(toCard.getCardNumber()),
                amount, description
        );
    }

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     *
     * @param number полный номер карты
     * @return маскированный номер карты
     */
    private String maskCardNumber(String number) {
        return "**** **** **** " +
                number.substring(number.length() - 4);
    }

    /**
     * Проверяет статус карты на блокировку или истечение срока действия.
     *
     * @param card карта для проверки
     */
    private void validateCard(Card card) {
        if (card.getStatus().equals(CardStatus.BLOCKED)) {
            throw new IllegalStateException("Card is blocked");
        }
        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Card is expired");
        }
    }

    /**
     * Проверяет превышение дневного лимита снятия средств.
     *
     * @param card      карта, с которой производится снятие
     * @param newAmount сумма новой операции
     */
    private void validateLimit(Card card, BigDecimal newAmount) {
        Limit limit = limitRepository.findByName("daily_limit");


        if (limit == null) {
            limit = new Limit();
            limit.setName("daily_limit");
            limit.setLimitValue(BigDecimal.valueOf(1000000.00));
            limit = limitRepository.save(limit);
        }

        BigDecimal dailyLimit = limit.getLimitValue();
        // Сумма всех операций WITHDRAW по этой карте за сегодняшний день
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        BigDecimal totalWithdrawToday = transactionRepository
                .sumWithdrawalsByCardAndDateRange(card, startOfDay, endOfDay)
                .orElse(BigDecimal.ZERO);

        BigDecimal projectedTotal = totalWithdrawToday.add(newAmount);

        if (projectedTotal.compareTo(dailyLimit) > 0) {
            throw new IllegalArgumentException("Daily withdrawal limit exceeded");
        }
    }

    /**
     * Получает карту по идентификатору и проверяет право доступа текущего пользователя.
     *
     * @param cardId идентификатор карты
     * @param user   текущий пользователь
     * @return найденная карта
     */
    private Card getCardForCurrentUser(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can't access someone else's card");
        }
        return card;
    }

    /**
     * Преобразует сущность транзакции в DTO с маскировкой номера карты.
     *
     * @param tx транзакция
     * @return DTO транзакции
     */
    public TransactionResponseDto mapToDto(Transaction tx) {
        TransactionResponseDto dto = modelMapper.map(tx, TransactionResponseDto.class);
        dto.setCardNumber(maskCardNumber(tx.getCard().getCardNumber()));

        return dto;
    }

}
