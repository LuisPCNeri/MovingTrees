package pt.ua.MovingTree.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.exceptions.InvalidCredentialsException;
import pt.ua.MovingTree.repositories.AppUserRepository;

@Service
@Transactional(readOnly = true)
public class UserSignInService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSignInService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse signIn(String username, String rawPassword) {
        AppUser user = userRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);
        if (rawPassword == null || user.getStatus() != AppUserStatus.ACTIVE
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return UserResponse.from(user);
    }
}
