package ru.dzhenbaz.BackendBankCardsManaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностями {@link User}.
 * Предоставляет методы для поиска пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
}
