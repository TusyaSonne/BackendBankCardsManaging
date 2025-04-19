package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import ru.dzhenbaz.BackendBankCardsManaging.dto.CardResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.CardRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserService userService;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private CardService cardService;

    private User user;
    private User admin;
    private Card card;
    private CardResponseDto cardDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setRole(Role.ROLE_USER);

        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ROLE_ADMIN);

        card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setOwner(user);
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));

        cardDto = new CardResponseDto();
        cardDto.setId(1L);
        cardDto.setMaskedCardNumber("**** **** **** 3456");
        cardDto.setBalance(BigDecimal.valueOf(1000));
        cardDto.setOwnerId(1L);
        cardDto.setStatus("ACTIVE");
    }

    @Test
    void shouldGetCardByIdForOwner() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, CardResponseDto.class)).thenReturn(cardDto);

        Optional<CardResponseDto> result = cardService.getById(1L, user);

        assertTrue(result.isPresent());
        assertEquals(cardDto.getId(), result.get().getId());
    }

    @Test
    void shouldNotGetCardIfNotOwner() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setRole(Role.ROLE_USER);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.getById(1L, anotherUser));
    }

    @Test
    void shouldAllowAdminToAccessAnyCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, CardResponseDto.class)).thenReturn(cardDto);

        Optional<CardResponseDto> result = cardService.getById(1L, admin);

        assertTrue(result.isPresent());
        assertEquals(cardDto.getId(), result.get().getId());
    }

    @Test
    void adminCanChangeCardStatusToActive() {
        card.setStatus(CardStatus.BLOCKED); // изначально карта заблокирована
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(modelMapper.map(card, CardResponseDto.class)).thenReturn(cardDto);

        Optional<CardResponseDto> result = cardService.changeCardStatus(1L, CardStatus.ACTIVE, admin);

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void userCanChangeCardStatusToBlocked() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(modelMapper.map(card, CardResponseDto.class)).thenReturn(cardDto);

        Optional<CardResponseDto> result = cardService.changeCardStatus(1L, CardStatus.BLOCKED, user);

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus()); // DTO ещё ACTIVE, но в логике меняется
        verify(cardRepository).save(card);
    }

    @Test
    void userCannotChangeCardStatusToActive() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cardService.changeCardStatus(1L, CardStatus.ACTIVE, user));

        assertEquals("You are only allowed to block your own cards.", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }
}

