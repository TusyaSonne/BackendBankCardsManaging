package ru.dzhenbaz.BackendBankCardsManaging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.dzhenbaz.BackendBankCardsManaging.dto.CardCreateRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.dto.RegisterRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.CardRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;
    private Long adminId;


    @BeforeEach
    void setup() {

        cardRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user = userRepository.save(user);

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole(Role.ROLE_ADMIN);
        admin = userRepository.save(admin);

        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail());
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail());


        this.adminId = admin.getId();
    }

    @Test
    void adminCanCreateCard() throws Exception {
        CardCreateRequestDto request = new CardCreateRequestDto(adminId, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/cards")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void userCannotCreateCard() throws Exception {
        CardCreateRequestDto request = new CardCreateRequestDto(1L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/cards")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanGetOnlyOwnCards() throws Exception {

        User user = userRepository.findUserByEmail("user@test.com").get();


        Card userCard = new Card();
        userCard.setOwner(user);
        userCard.setCardNumber("1234567890123456");
        userCard.setBalance(BigDecimal.valueOf(500));
        userCard.setExpirationDate(LocalDate.now().plusYears(2));
        userCard.setStatus(CardStatus.ACTIVE);
        cardRepository.save(userCard);

        User other = new User();
        other.setEmail("other@test.com");
        other.setPassword(passwordEncoder.encode("pass"));
        other.setRole(Role.ROLE_USER);
        other = userRepository.save(other);

        Card otherCard = new Card();
        otherCard.setOwner(other);
        otherCard.setCardNumber("6543210987654321");
        otherCard.setBalance(BigDecimal.valueOf(999));
        otherCard.setExpirationDate(LocalDate.now().plusYears(2));
        otherCard.setStatus(CardStatus.ACTIVE);
        cardRepository.save(otherCard);

        mockMvc.perform(get("/cards")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1)) // только одна карта
                .andExpect(jsonPath("$.content[0].balance").value(500))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 3456"));
    }

    @Test
    void userCanBlockOnlyOwnCardAndCannotSetOtherStatuses() throws Exception {

        User user = userRepository.findUserByEmail("user@test.com").get();

        Card card = new Card();
        card.setOwner(user);
        card.setCardNumber("1234567890123456");
        card.setBalance(BigDecimal.valueOf(1000));
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);

        mockMvc.perform(patch("/cards/" + card.getId() + "/status")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "newStatus": "BLOCKED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        mockMvc.perform(patch("/cards/" + card.getId() + "/status")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "newStatus": "ACTIVE"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You are only allowed to block your own cards."));
    }
}
