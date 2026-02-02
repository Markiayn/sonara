package ua.markiyan.sonara.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.markiyan.sonara.dto.request.UserRequest;
import ua.markiyan.sonara.dto.request.UserUpdateRequest;
import ua.markiyan.sonara.dto.response.UserResponse;
import ua.markiyan.sonara.entity.User;
import ua.markiyan.sonara.exception.NotFoundException;
import ua.markiyan.sonara.exception.ResourceAlreadyExistsException;
import ua.markiyan.sonara.mapper.UserMapper;
import ua.markiyan.sonara.repository.UserRepository;
import ua.markiyan.sonara.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public UserResponse create(UserRequest req) {
        if (repo.existsByEmailIgnoreCase(req.email())) {
            throw new ResourceAlreadyExistsException("email", "Користувач з такою поштою вже існує");
        }

        // мапимо DTO в ентіті
        User entity = UserMapper.toEntity(req);

        // тут шифруємо пароль перед збереженням
        entity.setPasswordHash(encoder.encode(req.password()));

        User saved = repo.save(entity);
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));
        return UserMapper.toResponse(u);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));

        if (req.email() != null && !req.email().isBlank()) u.setEmail(req.email());
        if (req.name() != null && !req.name().isBlank()) u.setName(req.name());
        if (req.country() != null) u.setCountry(req.country());

        User saved = repo.save(u);
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<UserResponse> search(String q, org.springframework.data.domain.Pageable pageable) {
        String pattern = (q == null) ? "" : q.trim();
        return repo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(pattern, pattern, pageable)
                .map(UserMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("User %d not found".formatted(id));
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User u = repo.findAll().stream()
                .filter(x -> x.getEmail() != null && x.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User with email %s not found".formatted(email)));
        return UserMapper.toResponse(u);
    }
}
