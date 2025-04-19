package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock private CardRepository cardRepository;
    @Mock private AuthService authService;
    @Mock private LimitRepository limitRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Card card;
    private Limit limit;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_USER);

        card = new Card();
        card.setId(10L);
        card.setOwner(user);
        card.setCardNumber("1111222233334444");
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setBalance(BigDecimal.valueOf(1000));

        limit = new Limit();
        limit.setName("daily_limit");
        limit.setLimitValue(BigDecimal.valueOf(500));
    }

    @Test
    void shouldWithdrawSuccessfullyWithinLimit() {
        when(authService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(limitRepository.findByName("daily_limit")).thenReturn(limit);
        when(transactionRepository.sumWithdrawalsByCardAndDateRange(any(), any(), any()))
                .thenReturn(Optional.of(BigDecimal.valueOf(100)));

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setCard(card);
        tx.setAmount(BigDecimal.valueOf(200));
        tx.setType(TransactionType.WITHDRAW);
        tx.setTimestamp(LocalDateTime.now());
        tx.setDescription("ATM");

        when(transactionRepository.save(any())).thenReturn(tx);

        WithdrawResponseDto mappedDto = new WithdrawResponseDto();
        mappedDto.setId(1L);
        mappedDto.setAmount(BigDecimal.valueOf(200));
        mappedDto.setType(TransactionType.WITHDRAW);
        mappedDto.setTimestamp(tx.getTimestamp());
        mappedDto.setDescription("ATM");
        mappedDto.setCardNumber("**** **** **** 4444");

        when(modelMapper.map(any(Transaction.class), eq(WithdrawResponseDto.class))).thenReturn(mappedDto);

        WithdrawResponseDto response = transactionService.withdraw(10L, BigDecimal.valueOf(200), "ATM");

        assertEquals(1L, response.getId());
        assertEquals("1111222233334444", response.getCardNumber());
        assertEquals(TransactionType.WITHDRAW, response.getType());
        assertEquals(BigDecimal.valueOf(200), response.getAmount());
        assertEquals("ATM", response.getDescription());
    }




    @Test
    void shouldThrowIfWithdrawExceedsBalance() {
        when(authService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        when(limitRepository.findByName("daily_limit"))
                .thenReturn(new Limit("daily_limit", BigDecimal.valueOf(10_000)));

        when(transactionRepository.sumWithdrawalsByCardAndDateRange(any(), any(), any()))
                .thenReturn(Optional.of(BigDecimal.ZERO));

        card.setBalance(BigDecimal.valueOf(100));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(10L, BigDecimal.valueOf(500), "Too much"));
    }


    @Test
    void shouldThrowIfWithdrawExceedsLimit() {
        when(authService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(limitRepository.findByName("daily_limit")).thenReturn(limit);
        when(transactionRepository.sumWithdrawalsByCardAndDateRange(any(), any(), any()))
                .thenReturn(Optional.of(BigDecimal.valueOf(400)));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(10L, BigDecimal.valueOf(200), "ATM"));
    }

    @Test
    void shouldTransferBetweenUserCards() {
        Card toCard = new Card();
        toCard.setId(11L);
        toCard.setOwner(user);
        toCard.setCardNumber("9999888877776666");
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpirationDate(LocalDate.now().plusYears(1));
        toCard.setBalance(BigDecimal.ZERO);

        when(authService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(11L)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                transactionService.transfer(10L, 11L, BigDecimal.valueOf(300), "Test transfer"));

        assertEquals(BigDecimal.valueOf(700), card.getBalance());
        assertEquals(BigDecimal.valueOf(300), toCard.getBalance());
    }

    @Test
    void shouldThrowIfTransferToSameCard() {
        when(authService.getCurrentUser()).thenReturn(user);
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(10L, 10L, BigDecimal.valueOf(100), "Same card"));
    }
}
