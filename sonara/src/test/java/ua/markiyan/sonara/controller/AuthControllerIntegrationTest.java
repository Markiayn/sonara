package ua.markiyan.sonara.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ua.markiyan.sonara.dto.request.UserRequest;
import ua.markiyan.sonara.repository.UserRepository;
import ua.markiyan.sonara.service.UserService;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== [SETUP] Початок створення тестового користувача ===");

        UserRequest signup = new UserRequest(
                "boss@sonara.com",
                "correct_password",
                "Boss",
                "Ukraine"
        );

        var createdUser = userService.create(signup);
        System.out.println("=== [SETUP] Користувача створено успішно: " + createdUser.email() + " ===");
    }

    @Test
    void shouldReturnToken_WhenCredentialsAreValid() throws Exception {
        System.out.println("\n--- [TEST] Старт тесту: Успішний логін ---");

        // Перевіримо, що юзер реально є в базі перед запитом
        userRepository.findByEmailIgnoreCase("boss@sonara.com").ifPresent(u -> {
            System.out.println("[DB CHECK] Юзер знайдений в БД. Статус: " + u.getStatus());
            System.out.println("[DB CHECK] Хеш пароля: " + u.getPasswordHash());
        });

        Map<String, String> loginReq = Map.of(
                "email", "boss@sonara.com",
                "password", "correct_password"
        );

        System.out.println("[REQUEST] Відправка POST запиту на /api/auth/login з email: " + loginReq.get("email"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andDo(result -> {
                    // Виводимо тіло відповіді, щоб побачити токен
                    String response = result.getResponse().getContentAsString();
                    int status = result.getResponse().getStatus();
                    System.out.println("[RESPONSE] Статус відповіді: " + status);
                    System.out.println("[RESPONSE] Тіло відповіді: " + (response.isEmpty() ? "ПУСТО" : response));
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        System.out.println("--- [TEST] Тест успішного логіну ЗАВЕРШЕНО ---");
    }


    @Test
    void shouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        System.out.println("\n--- [TEST] Старт тесту: Невалідні дані (401) ---");

        Map<String, String> invalidCredentials = Map.of(
                "email", "hacker@test.com",
                "password", "wrong_pass"
        );

        System.out.println("[REQUEST] Спроба логіну під неіснуючим юзером: " + invalidCredentials.get("email"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCredentials)))
                .andDo(result -> {
                    System.out.println("[RESPONSE] Очікуваний статус 401. Отримано: " + result.getResponse().getStatus());
                })
                .andExpect(status().isUnauthorized());

        System.out.println("--- [TEST] Тест 401 ЗАВЕРШЕНО ---");
    }
}