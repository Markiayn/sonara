package ua.markiyan.sonara.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.markiyan.sonara.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

}
