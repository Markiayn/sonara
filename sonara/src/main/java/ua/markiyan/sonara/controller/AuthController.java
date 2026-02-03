package ua.markiyan.sonara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import ua.markiyan.sonara.security.JwtUtil;
import ua.markiyan.sonara.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> req) {
        try {
            String email = req.get("email");
            String password = req.get("password");

            // 1. Спрінг перевіряє пароль
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

            // 2. Дістаємо дані користувача (включаючи нове поле role)
            var user = userService.findByEmail(email);

            // 3. Додаємо роль у токен разом із userId
            Map<String, Object> extraClaims = Map.of(
                    "userId", user.id(),
                    "role", user.role() // Тепер роль їде всередині JWT!
            );

            String token = jwtUtil.generateToken(user.email(), extraClaims);

            // 4. Повертаємо токен (можна ще й роль окремо для зручності фронта)
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", user.role()
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }
    }
}
