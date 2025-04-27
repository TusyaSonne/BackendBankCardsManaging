package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления пользователями.
 * Предоставляет методы получения пользователей и изменения их ролей.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Получает пользователя по идентификатору.
     * Доступно только администраторам.
     *
     * @param id идентификатор пользователя
     * @return пользователь, если найден
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Получает список всех пользователей системы.
     * Доступно только администраторам.
     *
     * @return список пользователей
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Изменяет роль пользователя.
     * Проверяет, чтобы администратор не мог изменить свою собственную роль на не-администратора.
     * Доступно только администраторам.
     *
     * @param userId      идентификатор пользователя
     * @param newRole     новая роль пользователя
     * @param currentUser текущий пользователь
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void changeUserRole(Long userId, Role newRole, User currentUser) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (targetUser.getId().equals(currentUser.getId()) && newRole != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You cannot change your own role to non-admin.");
        }

        if (targetUser.getRole() == newRole) {
            throw new IllegalArgumentException("User already has role " + newRole);
        }

        targetUser.setRole(newRole);
        userRepository.save(targetUser);
    }

}
