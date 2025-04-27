package ru.dzhenbaz.BackendBankCardsManaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;

import java.util.List;

/**
 * Репозиторий для работы с сущностями {@link Card}.
 * Предоставляет методы для поиска карт по владельцу и статусу.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByCardNumber(String cardNumber);

    List<Card> findAllByOwnerId(Long ownerId);

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);

    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    Page<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);
}
