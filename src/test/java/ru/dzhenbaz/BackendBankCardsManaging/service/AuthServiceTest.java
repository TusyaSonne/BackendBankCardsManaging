package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.dzhenbaz.BackendBankCardsManaging.dto.AuthResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LoginRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.RegisterRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerDto;
    private LoginRequestDto loginDto;
    private User user;

    @BeforeEach
    void setup() {
        registerDto = new RegisterRequestDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password");
        registerDto.setRole(Role.ROLE_USER);

        loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password");

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.findUserByEmail(registerDto.getEmail()))
                .thenReturn(Optional.empty()) //проверка того что пользователя нет в бд
                .thenReturn(Optional.of(user)); //возвращение уже сохраненного пользователя

        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(registerDto.getEmail())).thenReturn("mocked-jwt");

        AuthResponseDto response = authService.register(registerDto);

        assertEquals("mocked-jwt", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNotRegisterDuplicateEmail() {
        when(userRepository.findUserByEmail(registerDto.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginUserWithCorrectCredentials() {
        when(userRepository.findUserByEmail(loginDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("mocked-jwt");

        AuthResponseDto response = authService.login(loginDto);

        assertEquals("mocked-jwt", response.getToken());
    }

    @Test
    void shouldNotLoginWithInvalidPassword() {
        when(userRepository.findUserByEmail(loginDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDto.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDto));
    }

    @Test
    void shouldNotLoginNonExistingUser() {
        when(userRepository.findUserByEmail(registerDto.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDto));
    }
}
