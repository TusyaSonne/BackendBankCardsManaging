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
import ru.dzhenbaz.BackendBankCardsManaging.dto.ChangeRoleRequestDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.User;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.Role;
import ru.dzhenbaz.BackendBankCardsManaging.repository.UserRepository;
import ru.dzhenbaz.BackendBankCardsManaging.security.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private Long userId;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail());

        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("userpass"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void adminCanGetAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void adminCanGetUserById() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void adminCanChangeUserRole() throws Exception {
        ChangeRoleRequestDto dto = new ChangeRoleRequestDto(Role.ROLE_ADMIN);

        mockMvc.perform(post("/users/" + userId + "/role")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("User role updated to ROLE_ADMIN"));
    }

    @Test
    void adminCannotDemoteSelfToUser() throws Exception {
        User admin = userRepository.findUserByEmail("admin@test.com").orElseThrow();
        ChangeRoleRequestDto dto = new ChangeRoleRequestDto(Role.ROLE_USER);

        mockMvc.perform(post("/users/" + admin.getId() + "/role")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You cannot change your own role to non-admin."));
    }
}
