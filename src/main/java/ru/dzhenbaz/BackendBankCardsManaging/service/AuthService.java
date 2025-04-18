package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.dzhenbaz.BackendBankCardsManaging.dto.AuthResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LoginRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.RegisterRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.ClientDetails;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository repository, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDto register(RegisterRequestDto request) {

        if (repository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
            // Возможно потом заменить на валидатор
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

    public AuthResponseDto login(LoginRequestDto request) {
        User user = repository.findUserByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException(("User with this email not found")));
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        return new AuthResponseDto((jwtUtil.generateToken(user.getEmail())));
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ClientDetails clientDetails = (ClientDetails) authentication.getPrincipal();

        return clientDetails.getUser();
    }
}
