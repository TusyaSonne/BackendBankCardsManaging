package ru.dzhenbaz.BackendBankCardsManaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.LimitRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LimitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private LimitRepository limitRepository;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setup() {
        limitRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail());
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail());
    }

    @Test
    void userCanViewDailyLimit() throws Exception {
        mockMvc.perform(get("/limits/daily")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("daily_limit"))
                .andExpect(jsonPath("$.value").isNumber());
    }

    @Test
    void adminCanUpdateDailyLimit() throws Exception {
        mockMvc.perform(post("/limits/daily")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newValue": 100000.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(100000.00));
    }

    @Test
    void userCannotUpdateDailyLimit() throws Exception {
        mockMvc.perform(post("/limits/daily")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newValue": 12345.67
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
