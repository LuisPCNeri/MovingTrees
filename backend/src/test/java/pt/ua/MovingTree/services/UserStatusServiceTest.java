package pt.ua.MovingTree.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ua.MovingTree.dtos.UpdateUserStatusRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.entities.Municipality;
import pt.ua.MovingTree.entities.Team;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.security.CallerGuard;

@ExtendWith(MockitoExtension.class)
class UserStatusServiceTest {
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private CallerGuard callerGuard;
    @InjectMocks
    private UserStatusService service;

    //passa, INACTIVE -> ACTIVE grava o novo estado
    @Test
    void activatesUser() {
        AppUser user = user(AppUserStatus.INACTIVE);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));
        when(userRepository.save(eq(10L), eq(user))).thenReturn(user);

        UserResponse response = service.changeStatus(30L, new UpdateUserStatusRequest(AppUserStatus.ACTIVE));

        assertEquals(AppUserStatus.ACTIVE, user.getStatus());
        assertEquals(AppUserStatus.ACTIVE, response.status());
        verify(userRepository).save(10L, user);
    }

    //passa, ACTIVE -> INACTIVE grava o novo estado
    @Test
    void deactivatesUser() {
        AppUser user = user(AppUserStatus.ACTIVE);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));
        when(userRepository.save(eq(10L), eq(user))).thenReturn(user);

        UserResponse response = service.changeStatus(30L, new UpdateUserStatusRequest(AppUserStatus.INACTIVE));

        assertEquals(AppUserStatus.INACTIVE, user.getStatus());
        assertEquals(AppUserStatus.INACTIVE, response.status());
        verify(userRepository).save(10L, user);
    }

    //passa, repetir o mesmo status é idempotente e não grava
    @Test
    void repeatedStatusIsIdempotent() {
        AppUser user = user(AppUserStatus.ACTIVE);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));

        UserResponse response = service.changeStatus(30L, new UpdateUserStatusRequest(AppUserStatus.ACTIVE));

        assertEquals(AppUserStatus.ACTIVE, user.getStatus());
        assertEquals(AppUserStatus.ACTIVE, response.status());
        verify(userRepository, never()).save(any(), any());
    }

    private static AppUser user(AppUserStatus status) {
        Municipality municipality = new Municipality("Municipality 10");
        ReflectionTestUtils.setField(municipality, "id", 10L);
        Team team = new Team(municipality, "Parks");
        ReflectionTestUtils.setField(team, "id", 20L);
        AppUser user = new AppUser(municipality, team, "Ana", "ana.status", "hash", AppUserRole.FIELD_WORKER);
        ReflectionTestUtils.setField(user, "id", 30L);
        ReflectionTestUtils.setField(user, "status", status);
        return user;
    }
}
