package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.dto.CardResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.CardRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;


    @Autowired
    public CardService(CardRepository cardRepository, UserService userService, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto createCard(Long id, BigDecimal balance) {
        User user = userService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Card card = new Card();
        card.setOwner(user);
        card.setCardNumber(generateCardNumber());
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(balance);

        Card saveCard = cardRepository.save(card);
        return mapToCardDto(saveCard);
    }

    public Optional<CardResponseDto> getById(Long id, User currentUser) {
        Optional<Card> cardOpt = cardRepository.findById(id);

        if (cardOpt.isEmpty()) {
            return Optional.empty();
        }

        Card card = cardOpt.get();

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to access this card.");
        }

        return Optional.of(mapToCardDto(card));
    }

    public Page<CardResponseDto> getAllCards(User currentUser, CardStatus status, Pageable pageable) {
        Page<Card> cards;

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            cards = (status != null)
                    ? cardRepository.findAllByStatus(status, pageable)
                    : cardRepository.findAll(pageable);
        } else {
            cards = (status != null)
                    ? cardRepository.findAllByOwnerIdAndStatus(currentUser.getId(), status, pageable)
                    : cardRepository.findAllByOwnerId(currentUser.getId(), pageable);
        }

        return cards.map(this::mapToCardDto);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(Long id) {

        cardRepository.deleteById(id);
    }

    @Transactional
    public Optional<CardResponseDto> changeCardStatus(Long id, CardStatus newStatus, User currentUser) {
        Optional<Card> cardOpt = cardRepository.findById(id);
        if (cardOpt.isEmpty()) {
            return Optional.empty();
        }

        Card card = cardOpt.get();

        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            return Optional.of(mapToCardDto(card));
        }

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            if (!card.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not allowed to modify this card.");
            }
            if (newStatus != CardStatus.BLOCKED) {
                throw new IllegalArgumentException("You are only allowed to block your own cards.");
            }
        }

        card.setStatus(newStatus);
        cardRepository.save(card);
        return Optional.of(mapToCardDto(card));
    }

    private String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        String cardNumber;

        do {
            StringBuilder number = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                number.append(random.nextInt(10));
            }
            cardNumber = number.toString();
        } while (cardRepository.existsByCardNumber(cardNumber));

        return cardNumber;
    }

    public CardResponseDto mapToCardDto(Card card) {
        CardResponseDto dto = modelMapper.map(card, CardResponseDto.class);
        dto.setMaskedCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setOwnerId(card.getOwner().getId());
        return dto;
    }

    private String maskCardNumber(String number) {
        return "**** **** **** " + number.substring(number.length() - 4);
    }

}
