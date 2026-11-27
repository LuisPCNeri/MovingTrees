package pt.ua.MovingTree.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.PagedResponse;
import pt.ua.MovingTree.dtos.UserFilter;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.exceptions.InvalidPagingException;
import pt.ua.MovingTree.exceptions.UserNotFoundException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.repositories.AppUserSpecifications;
import pt.ua.MovingTree.security.CallerGuard;

@Service
@Transactional(readOnly = true)
public class UserQueryService {
    public static final int MAX_PAGE_SIZE = 100;

    private final AppUserRepository userRepository;
    private final CallerGuard callerGuard;

    public UserQueryService(AppUserRepository userRepository, CallerGuard callerGuard) {
        this.userRepository = userRepository;
        this.callerGuard = callerGuard;
    }

    public PagedResponse<UserResponse> list(int page, int size, UserFilter filter) {
        if (page < 0) {
            throw new InvalidPagingException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPagingException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        Long municipalityId = callerGuard.requireMunicipalAdministrator();
        Page<AppUser> users = userRepository.findAll(
                AppUserSpecifications.forListing(municipalityId, filter),
                PageRequest.of(page, size, Sort.by("id")));
        return PagedResponse.from(users.map(UserResponse::from));
    }

    public UserResponse getById(long id) {
        Long municipalityId = callerGuard.requireMunicipalAdministrator();
        return userRepository.findByMunicipality_IdAndId(municipalityId, id)
                .map(UserResponse::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
