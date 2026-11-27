package pt.ua.MovingTree.dtos;

import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;

public record UserFilter(AppUserRole role, Long teamId, AppUserStatus status, String name) {
}
