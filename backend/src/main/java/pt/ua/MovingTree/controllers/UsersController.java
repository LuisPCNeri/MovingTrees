package pt.ua.MovingTree.controllers;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.ua.MovingTree.dtos.CreateUserRequest;
import pt.ua.MovingTree.dtos.PagedResponse;
import pt.ua.MovingTree.dtos.UserFilter;
import pt.ua.MovingTree.dtos.UpdateUserRequest;
import pt.ua.MovingTree.dtos.UpdateUserStatusRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.services.UserQueryService;
import pt.ua.MovingTree.services.UserRegistrationService;
import pt.ua.MovingTree.services.UserStatusService;
import pt.ua.MovingTree.services.UserUpdateService;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserRegistrationService registrationService;
    private final UserQueryService queryService;
    private final UserUpdateService updateService;
    private final UserStatusService statusService;

    public UsersController(UserRegistrationService registrationService, UserQueryService queryService,
            UserUpdateService updateService, UserStatusService statusService) {
        this.registrationService = registrationService;
        this.queryService = queryService;
        this.updateService = updateService;
        this.statusService = statusService;
    }

    @GetMapping
    public PagedResponse<UserResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AppUserRole role,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) AppUserStatus status,
            @RequestParam(required = false) String name) {
        return queryService.list(page, size, new UserFilter(role, teamId, status, name));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable long id) {
        return queryService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable long id, @Valid @RequestBody UpdateUserRequest request) {
        return updateService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(@PathVariable long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return statusService.changeStatus(id, request);
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = registrationService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
