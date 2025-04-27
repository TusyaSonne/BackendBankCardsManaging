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
import java.util.Optional;

/**
 * Сервис для управления банковскими картами.
 * Реализует бизнес-логику создания, удаления, изменения статуса и получения карт.
 */
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

    /**
     * Создает новую карту для пользователя с указанным балансом.
     * Доступно только администраторам.
     *
     * @param id      идентификатор пользователя
     * @param balance начальный баланс карты
     * @return созданная карта в виде DTO
     */
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

    /**
     * Получает карту по ID, проверяя права доступа текущего пользователя.
     *
     * @param id          идентификатор карты
     * @param currentUser текущий пользователь
     * @return найденная карта в виде DTO, либо пустой Optional
     */
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

    /**
     * Получает список карт с фильтрацией по статусу и пагинацией.
     * Доступные карты зависят от роли пользователя.
     *
     * @param currentUser текущий пользователь
     * @param status      статус карты (опционально)
     * @param pageable    параметры пагинации
     * @return страница карт
     */
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

    /**
     * Удаляет карту по её идентификатору.
     * Доступно только администраторам.
     *
     * @param id идентификатор карты
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(Long id) {

        cardRepository.deleteById(id);
    }

    /**
     * Изменяет статус карты.
     * Проверяет права пользователя.
     *
     * @param id          идентификатор карты
     * @param newStatus   новый статус карты
     * @param currentUser текущий пользователь
     * @return обновленная карта в виде DTO, либо пустой Optional
     */
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

    /**
     * Генерирует уникальный номер карты из 16 цифр.
     *
     * @return уникальный номер карты
     */
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

    /**
     * Преобразует сущность карты в DTO.
     *
     * @param card сущность карты
     * @return DTO карты
     */
    public CardResponseDto mapToCardDto(Card card) {
        CardResponseDto dto = modelMapper.map(card, CardResponseDto.class);
        dto.setMaskedCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setOwnerId(card.getOwner().getId());
        return dto;
    }

    /**
     * Маскирует номер карты, скрывая все цифры кроме последних четырех.
     *
     * @param number полный номер карты
     * @return маскированный номер карты
     */
    private String maskCardNumber(String number) {
        return "**** **** **** " + number.substring(number.length() - 4);
    }

}
