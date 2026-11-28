package pt.ua.MovingTree.dtos;

import jakarta.validation.constraints.NotNull;
import pt.ua.MovingTree.enums.AppUserStatus;

public record UpdateUserStatusRequest(@NotNull AppUserStatus status) {
}
