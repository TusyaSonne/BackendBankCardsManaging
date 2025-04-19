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
import ru.dzhenbaz.BackendBankCardsManaging.dto.WithdrawRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Card;
import ru.dzhenbaz.BackendBankCardsManaging.model.Limit;
import ru.dzhenbaz.BackendBankCardsManaging.model.Transaction;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;
import ru.dzhenbaz.BackendBankCardsManaging.repository.CardRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.LimitRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.TransactionRepository;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private LimitRepository limitRepository;

    private String userToken;
    private String adminToken;
    private Long userCardId1;
    private Long userCardId2;
    private Long adminCardId;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
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

        // Карты пользователя
        Card card1 = new Card();
        card1.setOwner(user);
        card1.setCardNumber("1111222233334444");
        card1.setExpirationDate(LocalDate.now().plusYears(2));
        card1.setStatus(CardStatus.ACTIVE);
        card1.setBalance(BigDecimal.valueOf(10000));
        cardRepository.save(card1);
        userCardId1 = card1.getId();

        Card card2 = new Card();
        card2.setOwner(user);
        card2.setCardNumber("5555666677778888");
        card2.setExpirationDate(LocalDate.now().plusYears(2));
        card2.setStatus(CardStatus.ACTIVE);
        card2.setBalance(BigDecimal.ZERO);
        cardRepository.save(card2);
        userCardId2 = card2.getId();

        //Карта администратора
        Card adminCard = new Card();
        adminCard.setOwner(admin);
        adminCard.setCardNumber("9999888877776666");
        adminCard.setExpirationDate(LocalDate.now().plusYears(2));
        adminCard.setStatus(CardStatus.ACTIVE);
        adminCard.setBalance(BigDecimal.valueOf(2000));
        cardRepository.save(adminCard);
        adminCardId = adminCard.getId();
    }

    @Test
    void userCanWithdrawWithinBalanceAndLimit() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "cardId": %d,
                                    "amount": 500.00,
                                    "description": "ATM"
                                }
                                """.formatted(userCardId1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("ATM"));
    }

    @Test
    void userCannotWithdrawMoreThanBalance() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "cardId": %d,
                                    "amount": 20000.00,
                                    "description": "Attempt overdraft"
                                }
                                """.formatted(userCardId1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCannotWithdrawMoreThanDailyLimit() throws Exception {
        User user = userRepository.findUserByEmail("user@test.com").orElseThrow();
        Card card = cardRepository.findAll().stream()
                .filter(c -> c.getOwner().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();


        Limit limit = limitRepository.findByName("daily_limit");
        limit.setLimitValue(BigDecimal.valueOf(1000));
        limitRepository.save(limit);

        card.setBalance(BigDecimal.valueOf(2000));
        cardRepository.save(card);

        WithdrawRequestDto request = new WithdrawRequestDto(card.getId(), BigDecimal.valueOf(1500), "Over limit");

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Daily withdrawal limit exceeded")));
    }

    @Test
    void userCanTransferBetweenOwnCards() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fromCardId": %d,
                                    "toCardId": %d,
                                    "amount": 300.00,
                                    "description": "To my second card"
                                }
                                """.formatted(userCardId1, userCardId2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.fromCardNumber").exists())
                .andExpect(jsonPath("$.toCardNumber").exists());
    }

    @Test
    void userCannotTransferToSameCard() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fromCardId": %d,
                                    "toCardId": %d,
                                    "amount": 300.00,
                                    "description": "Same card"
                                }
                                """.formatted(userCardId1, userCardId1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userSeesOnlyOwnTransactions() throws Exception {

        User user = userRepository.findUserByEmail("user@test.com").get();
        User admin = userRepository.findUserByEmail("admin@test.com").get();

        Card userCard = cardRepository.findAll().stream()
                .filter(card -> card.getOwner().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();

        Card adminCard = cardRepository.findAll().stream()
                .filter(card -> card.getOwner().getId().equals(admin.getId()))
                .findFirst()
                .orElseThrow();

        Transaction tx = new Transaction();
        tx.setCard(userCard);
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setType(TransactionType.WITHDRAW);
        tx.setTimestamp(LocalDateTime.now());
        tx.setDescription("User Transaction");
        transactionRepository.save(tx);

        Transaction tx2 = new Transaction();
        tx2.setCard(adminCard);
        tx2.setAmount(BigDecimal.valueOf(500));
        tx2.setType(TransactionType.WITHDRAW);
        tx2.setTimestamp(LocalDateTime.now());
        tx2.setDescription("Admin Transaction");
        transactionRepository.save(tx2);

        mockMvc.perform(get("/transactions")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].description").value("User Transaction"));
    }

    @Test
    void adminSeesAllTransactions() throws Exception {

        User user = userRepository.findUserByEmail("user@test.com").get();
        User admin = userRepository.findUserByEmail("admin@test.com").get();

        Card userCard = cardRepository.findAll().stream()
                .filter(card -> card.getOwner().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();

        Card adminCard = cardRepository.findAll().stream()
                .filter(card -> card.getOwner().getId().equals(admin.getId()))
                .findFirst()
                .orElseThrow();


        Transaction tx1 = new Transaction();
        tx1.setCard(userCard);
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setType(TransactionType.WITHDRAW);
        tx1.setTimestamp(LocalDateTime.now());
        tx1.setDescription("User Transaction");
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setCard(adminCard);
        tx2.setAmount(BigDecimal.valueOf(500));
        tx2.setType(TransactionType.WITHDRAW);
        tx2.setTimestamp(LocalDateTime.now());
        tx2.setDescription("Admin Transaction");
        transactionRepository.save(tx2);

        mockMvc.perform(get("/transactions")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
}
