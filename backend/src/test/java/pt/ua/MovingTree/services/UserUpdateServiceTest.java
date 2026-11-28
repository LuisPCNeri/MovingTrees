package pt.ua.MovingTree.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ua.MovingTree.dtos.UpdateUserRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.entities.Municipality;
import pt.ua.MovingTree.entities.Team;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.exceptions.InvalidUserUpdateException;
import pt.ua.MovingTree.exceptions.TeamNotInMunicipalityException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.repositories.TeamRepository;
import pt.ua.MovingTree.security.CallerGuard;

@ExtendWith(MockitoExtension.class)
class UserUpdateServiceTest {
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CallerGuard callerGuard;
    @InjectMocks
    private UserUpdateService service;

    //passa, alterar apenas o role preserva o team atual
    @Test
    void changesRoleAndKeepsTeam() {
        AppUser user = user(10L, 20L, AppUserRole.FIELD_WORKER);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));
        when(userRepository.save(eq(10L), eq(user))).thenReturn(user);

        UserResponse response = service.update(30L, new UpdateUserRequest(AppUserRole.FLEET_OPERATOR, null));

        assertEquals(AppUserRole.FLEET_OPERATOR, user.getRole());
        assertEquals(20L, user.getTeam().getId());
        assertEquals(AppUserRole.FLEET_OPERATOR, response.role());
        verify(userRepository).save(10L, user);
        verifyNoInteractions(teamRepository);
    }

    //passa, alterar apenas o team preserva o role atual
    @Test
    void changesTeamAndKeepsRole() {
        AppUser user = user(10L, 20L, AppUserRole.FIELD_WORKER);
        Team newTeam = team(10L, 21L);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));
        when(teamRepository.findByMunicipality_IdAndId(10L, 21L)).thenReturn(Optional.of(newTeam));
        when(userRepository.save(eq(10L), eq(user))).thenReturn(user);

        UserResponse response = service.update(30L, new UpdateUserRequest(null, 21L));

        assertEquals(21L, user.getTeam().getId());
        assertEquals(AppUserRole.FIELD_WORKER, user.getRole());
        assertEquals(21L, response.teamId());
        verify(userRepository).save(10L, user);
    }

    //falha, team de outro município deve ser recusado sem alterar o utilizador
    @Test
    void rejectsTeamFromAnotherMunicipality() {
        AppUser user = user(10L, 20L, AppUserRole.FIELD_WORKER);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));
        when(teamRepository.findByMunicipality_IdAndId(10L, 99L)).thenReturn(Optional.empty());

        assertThrows(TeamNotInMunicipalityException.class,
                () -> service.update(30L, new UpdateUserRequest(AppUserRole.FLEET_OPERATOR, 99L)));

        assertEquals(AppUserRole.FIELD_WORKER, user.getRole());
        assertEquals(20L, user.getTeam().getId());
        verify(userRepository, never()).save(any(), any());
    }

    //passa, valores iguais aos atuais não disparam gravação nem consulta de team
    @Test
    void doesNotChangeWhenValuesAreIdentical() {
        AppUser user = user(10L, 20L, AppUserRole.FIELD_WORKER);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.findByMunicipality_IdAndId(10L, 30L)).thenReturn(Optional.of(user));

        UserResponse response = service.update(30L, new UpdateUserRequest(AppUserRole.FIELD_WORKER, 20L));

        assertEquals(AppUserRole.FIELD_WORKER, user.getRole());
        assertEquals(20L, user.getTeam().getId());
        assertEquals(AppUserRole.FIELD_WORKER, response.role());
        verify(userRepository, never()).save(any(), any());
        verifyNoInteractions(teamRepository);
    }

    //falha, um pedido sem role e sem team não é uma alteração
    @Test
    void rejectsUpdateWithoutChanges() {
        assertThrows(InvalidUserUpdateException.class,
                () -> service.update(30L, new UpdateUserRequest(null, null)));

        verifyNoInteractions(callerGuard, userRepository, teamRepository);
    }

    private static AppUser user(long municipalityId, long teamId, AppUserRole role) {
        Municipality municipality = new Municipality("Municipality " + municipalityId);
        ReflectionTestUtils.setField(municipality, "id", municipalityId);
        Team team = team(municipality, teamId);
        AppUser user = new AppUser(municipality, team, "Ana", "ana.update", "hash", role);
        ReflectionTestUtils.setField(user, "id", 30L);
        return user;
    }

    private static Team team(long municipalityId, long teamId) {
        Municipality municipality = new Municipality("Municipality " + municipalityId);
        ReflectionTestUtils.setField(municipality, "id", municipalityId);
        return team(municipality, teamId);
    }

    private static Team team(Municipality municipality, long teamId) {
        Team team = new Team(municipality, "Team " + teamId);
        ReflectionTestUtils.setField(team, "id", teamId);
        return team;
    }
}
