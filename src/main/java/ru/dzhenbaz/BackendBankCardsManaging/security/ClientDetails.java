package ru.dzhenbaz.BackendBankCardsManaging.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Реализация {@link UserDetails} для интеграции пользовательской сущности {@link User} с Spring Security.
 * Используется для аутентификации и авторизации в системе.
 */
public class ClientDetails implements UserDetails {


    private final User user;

    public ClientDetails(User user) {
        this.user = user;
    }

    /**
     * Возвращает роль пользователя в формате GrantedAuthority.
     *
     * @return коллекция прав пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    /**
     * Возвращает зашифрованный пароль пользователя.
     *
     * @return пароль
     */
    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    /**
     * Возвращает email пользователя в качестве логина.
     *
     * @return email
     */
    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return this.user;
    }
}
