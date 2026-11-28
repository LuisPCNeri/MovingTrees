package pt.ua.MovingTree.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.CreateUserRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.entities.Team;
import pt.ua.MovingTree.exceptions.DuplicateUsernameException;
import pt.ua.MovingTree.exceptions.TeamNotInMunicipalityException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.repositories.TeamRepository;
import pt.ua.MovingTree.security.CallerGuard;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional
public class UserRegistrationService {
    private final AppUserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final CallerGuard callerGuard;

    public UserRegistrationService(AppUserRepository userRepository, TeamRepository teamRepository, PasswordEncoder passwordEncoder, CallerGuard callerGuard) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.callerGuard = callerGuard;
    }

    public UserResponse register(CreateUserRequest request) {
        Long municipalityId = callerGuard.requireMunicipalAdministrator();
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }
        Team team = teamRepository.findByMunicipality_IdAndId(municipalityId, request.teamId())
                .orElseThrow(() -> new TeamNotInMunicipalityException(request.teamId(), municipalityId));
        AppUser user = new AppUser(
                team.getMunicipality(),
                team,
                request.name(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role());
        return UserResponse.from(userRepository.save(municipalityId, user));
    }
}
