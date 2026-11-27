package pt.ua.MovingTree.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.UpdateUserStatusRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.exceptions.InvalidUserUpdateException;
import pt.ua.MovingTree.exceptions.UserNotFoundException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.security.CallerGuard;

@Service
@Transactional
public class UserStatusService {
    private final AppUserRepository userRepository;
    private final CallerGuard callerGuard;

    public UserStatusService(AppUserRepository userRepository, CallerGuard callerGuard) {
        this.userRepository = userRepository;
        this.callerGuard = callerGuard;
    }

    public UserResponse changeStatus(long id, UpdateUserStatusRequest request) {
        if (request == null || request.status() == null) {
            throw new InvalidUserUpdateException("status must be provided");
        }
        Long municipalityId = callerGuard.requireMunicipalAdministrator();
        AppUser user = userRepository.findByMunicipality_IdAndId(municipalityId, id)
                .orElseThrow(UserNotFoundException::new);
        if (user.getStatus() != request.status()) {
            user.changeStatus(request.status());
            userRepository.save(municipalityId, user);
        }
        return UserResponse.from(user);
    }
}
