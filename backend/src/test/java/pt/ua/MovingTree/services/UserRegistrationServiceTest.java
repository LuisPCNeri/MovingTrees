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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ua.MovingTree.dtos.CreateUserRequest;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.entities.Municipality;
import pt.ua.MovingTree.entities.Team;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.exceptions.DuplicateUsernameException;
import pt.ua.MovingTree.exceptions.ForbiddenOperationException;
import pt.ua.MovingTree.exceptions.TeamNotInMunicipalityException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.repositories.TeamRepository;
import pt.ua.MovingTree.security.CallerGuard;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CallerGuard callerGuard;

    @InjectMocks
    private UserRegistrationService service;

    //passa, utilizador criado INACTIVE, com município da sessão e password hashed; municipalityId do payload é ignorado
    @Test
    void createsInactiveUserWithHashedPasswordAndIgnoresPayloadMunicipality() {
        long municipalityId = 10L;
        Team team = team(municipalityId, 20L);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(municipalityId);
        when(teamRepository.findByMunicipality_IdAndId(municipalityId, 20L)).thenReturn(Optional.of(team));
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(eq(municipalityId), any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(1);
            ReflectionTestUtils.setField(user, "id", 30L);
            return user;
        });

        UserResponse response = service.register(new CreateUserRequest(
                "Ana", "ana.unit", "plain-password", AppUserRole.FIELD_WORKER, 20L, 999L));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(eq(municipalityId), captor.capture());
        AppUser saved = captor.getValue();
        assertEquals(AppUserStatus.INACTIVE, saved.getStatus());
        assertEquals(municipalityId, saved.getMunicipality().getId());
        assertEquals(20L, saved.getTeam().getId());
        assertEquals("hashed-password", saved.getPasswordHash());
        assertEquals(municipalityId, response.municipalityId());
        verify(passwordEncoder).encode("plain-password");
    }

    //falha, team de outro município não pode ser associado ao utilizador
    @Test
    void rejectsTeamFromAnotherMunicipality() {
        long municipalityId = 10L;
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(municipalityId);
        when(userRepository.existsByUsername("ana.unit")).thenReturn(false);
        when(teamRepository.findByMunicipality_IdAndId(municipalityId, 99L)).thenReturn(Optional.empty());

        assertThrows(TeamNotInMunicipalityException.class, () -> service.register(new CreateUserRequest(
                "Ana", "ana.unit", "password123", AppUserRole.FIELD_WORKER, 99L, null)));

        verify(userRepository, never()).save(any(), any());
        verifyNoInteractions(passwordEncoder);
    }

    //falha, apenas administradores municipais podem registar utilizadores
    @Test
    void rejectsNonAdministrator() {
        when(callerGuard.requireMunicipalAdministrator())
                .thenThrow(new ForbiddenOperationException("Only municipal administrators can register users"));

        assertThrows(ForbiddenOperationException.class, () -> service.register(new CreateUserRequest(
                "Ana", "ana.unit", "password123", AppUserRole.FIELD_WORKER, 20L, null)));

        verifyNoInteractions(userRepository, teamRepository, passwordEncoder);
    }

    //falha, username duplicado deve ser rejeitado antes de gravar ou fazer hash.
    @Test
    void rejectsDuplicateUsername() {
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        when(userRepository.existsByUsername("ana.unit")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> service.register(new CreateUserRequest(
                "Ana", "ana.unit", "password123", AppUserRole.FIELD_WORKER, 20L, null)));

        verify(userRepository, never()).save(any(), any());
        verifyNoInteractions(teamRepository, passwordEncoder);
    }

    private static Team team(long municipalityId, long teamId) {
        Municipality municipality = new Municipality("Municipality " + municipalityId);
        ReflectionTestUtils.setField(municipality, "id", municipalityId);
        Team team = new Team(municipality, "Parks");
        ReflectionTestUtils.setField(team, "id", teamId);
        return team;
    }
}
