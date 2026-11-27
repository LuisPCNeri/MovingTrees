package pt.ua.MovingTree.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.UpdateUserRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.entities.Team;
import pt.ua.MovingTree.exceptions.InvalidUserUpdateException;
import pt.ua.MovingTree.exceptions.TeamNotInMunicipalityException;
import pt.ua.MovingTree.exceptions.UserNotFoundException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.repositories.TeamRepository;
import pt.ua.MovingTree.security.CallerGuard;

@Service
@Transactional
public class UserUpdateService {
    private final AppUserRepository userRepository;
    private final TeamRepository teamRepository;
    private final CallerGuard callerGuard;

    public UserUpdateService(AppUserRepository userRepository, TeamRepository teamRepository, CallerGuard callerGuard) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.callerGuard = callerGuard;
    }

    public UserResponse update(long id, UpdateUserRequest request) {
        if (request == null || (request.role() == null && request.teamId() == null)) {
            throw new InvalidUserUpdateException("At least one of role or teamId must be provided");
        }
        Long municipalityId = callerGuard.requireMunicipalAdministrator();
        AppUser user = userRepository.findByMunicipality_IdAndId(municipalityId, id)
                .orElseThrow(UserNotFoundException::new);

        Team newTeam = null;
        if (request.teamId() != null && !request.teamId().equals(user.getTeam().getId())) {
            newTeam = teamRepository.findByMunicipality_IdAndId(municipalityId, request.teamId())
                    .orElseThrow(() -> new TeamNotInMunicipalityException(request.teamId(), municipalityId));
        }

        boolean changed = false;
        if (request.role() != null && request.role() != user.getRole()) {
            user.changeRole(request.role());
            changed = true;
        }
        if (newTeam != null) {
            user.changeTeam(newTeam);
            changed = true;
        }
        if (changed) {
            userRepository.save(municipalityId, user);
        }
        return UserResponse.from(user);
    }
}
