package pt.ua.MovingTree.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.ua.MovingTree.enums.AppUserRole;

public record CreateUserRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 72) String password,
        @NotNull AppUserRole role,
        @NotNull Long teamId,
        Long municipalityId) {
}
