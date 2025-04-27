package ru.dzhenbaz.BackendBankCardsManaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dzhenbaz.BackendBankCardsManaging.model.Limit;

/**
 * Репозиторий для работы с сущностями {@link Limit}.
 * Предоставляет методы для поиска лимитов по имени.
 */
@Repository
public interface LimitRepository extends JpaRepository<Limit, Integer> {
    Limit findByName(String name);
}
