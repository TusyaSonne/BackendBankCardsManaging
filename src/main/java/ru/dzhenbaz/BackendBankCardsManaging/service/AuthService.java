package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.dto.AuthResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LoginRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.RegisterRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.ClientDetails;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

/**
 * Сервис для аутентификации и регистрации пользователей.
 * Отвечает за регистрацию новых пользователей, выдачу JWT токенов при входе и получение текущего пользователя.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param repository репозиторий пользователей
     * @param encoder    шифратор паролей
     * @param jwtUtil    утилита для генерации JWT токенов
     */
    @Autowired
    public AuthService(UserRepository repository, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные для регистрации
     * @return токен авторизации
     * @throws IllegalArgumentException если пользователь с таким email уже существует
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        if (repository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        repository.save(user);

        User savedUser = repository.findUserByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User with this email not found"));

        return new AuthResponseDto(jwtUtil.generateToken(savedUser.getEmail()));
    }

    /**
     * Аутентифицирует пользователя по email и паролю.
     *
     * @param request данные для входа
     * @return токен авторизации
     * @throws BadCredentialsException если данные неверные
     */
    public AuthResponseDto login(LoginRequestDto request) {
        User user = repository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return new AuthResponseDto((jwtUtil.generateToken(user.getEmail())));
    }

    /**
     * Возвращает текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ClientDetails clientDetails = (ClientDetails) authentication.getPrincipal();

        return clientDetails.getUser();
    }
}
