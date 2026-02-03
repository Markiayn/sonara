package ua.markiyan.sonara.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.markiyan.sonara.entity.User;
import ua.markiyan.sonara.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Використовуємо нормальний пошук
        User u = repo.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(username)));

        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                // ТЕПЕР БЕРЕМО РОЛЬ З БАЗИ:
                .authorities(new SimpleGrantedAuthority(u.getRole().name()))
                .disabled(u.getStatus() != User.Status.ACTIVE)
                .build();
    }
}
