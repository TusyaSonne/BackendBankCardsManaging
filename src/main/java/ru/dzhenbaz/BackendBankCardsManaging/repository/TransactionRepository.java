package ru.dzhenbaz.BackendBankCardsManaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.Transaction;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByCard(Card card);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.card = :card AND t.type = 'WITHDRAW' " +
            "AND t.timestamp BETWEEN :start AND :end")
    Optional<BigDecimal> sumWithdrawalsByCardAndDateRange(
            @Param("card") Card card,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Transaction> findAllByCard_Owner(User owner);

    Page<Transaction> findAllByCard(Card card, Pageable pageable);

    Page<Transaction> findAllByCardAndType(Card card, TransactionType type, Pageable pageable);

    Page<Transaction> findAllByType(TransactionType type, Pageable pageable);

    Page<Transaction> findAllByCard_Owner(User owner, Pageable pageable);

    Page<Transaction> findAllByCard_OwnerAndType(User owner, TransactionType type, Pageable pageable);
}
