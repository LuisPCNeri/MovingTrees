package pt.ua.MovingTree.dtos;

import pt.ua.MovingTree.enums.AppUserRole;

public record UpdateUserRequest(AppUserRole role, Long teamId) {
}
