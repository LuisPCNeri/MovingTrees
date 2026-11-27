package pt.ua.MovingTree.dtos;

import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;

public record UserResponse(
        Long id,
        String name,
        String username,
        AppUserRole role,
        AppUserStatus status,
        Long municipalityId,
        Long teamId) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                user.getMunicipality().getId(),
                user.getTeam().getId());
    }
}
