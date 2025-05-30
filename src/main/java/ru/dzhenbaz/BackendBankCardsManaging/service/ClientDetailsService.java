package ru.dzhenbaz.BackendBankCardsManaging.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.ClientDetails;

import java.util.Optional;

/**
 * Сервис загрузки данных пользователя для Spring Security.
 * Используется для аутентификации пользователей по email.
 */
@Service
@Transactional(readOnly = true)
public class ClientDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ClientDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает пользователя по его email.
     *
     * @param username email пользователя
     * @return детали пользователя для Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findUserByEmail(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User with this email not found");
        }

        return new ClientDetails(user.get());
    }


}
