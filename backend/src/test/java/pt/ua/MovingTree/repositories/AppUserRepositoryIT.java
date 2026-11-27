package pt.ua.MovingTree.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.ua.MovingTree.support.IdentityTestData.municipality;
import static pt.ua.MovingTree.support.IdentityTestData.team;
import static pt.ua.MovingTree.support.IdentityTestData.user;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.entities.Municipality;
import pt.ua.MovingTree.entities.Team;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.support.PostgresIntegrationTest;

@SpringBootTest
@Transactional
class AppUserRepositoryIT extends PostgresIntegrationTest {
    @Autowired
    private AppUserRepository repository;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntityManager entityManager;

    //passa, consultar com o município A nunca deve devolver utilizadores do município B
    @Test
    void queriesNeverReturnUsersFromAnotherMunicipality() {
        long municipalityA = municipality(jdbc, "repo-isolation-a");
        long municipalityB = municipality(jdbc, "repo-isolation-b");
        long teamA = team(jdbc, municipalityA, "parks-a");
        long teamB = team(jdbc, municipalityB, "parks-b");
        long userA = user(jdbc, municipalityA, teamA, "Ana", "ana.repo-isolation", "FIELD_WORKER");
        long userB = user(jdbc, municipalityB, teamB, "Bruno", "bruno.repo-isolation", "FIELD_WORKER");

        assertTrue(repository.findByMunicipality_IdAndId(municipalityA, userB).isEmpty());
        assertTrue(repository.findByMunicipality_IdAndId(municipalityB, userA).isEmpty());
        assertTrue(repository.findByMunicipality_IdAndUsername(municipalityA, "bruno.repo-isolation").isEmpty());
        assertTrue(repository.findByMunicipality_IdAndUsername(municipalityB, "ana.repo-isolation").isEmpty());
        assertEquals(List.of("ana.repo-isolation"), usernames(repository.findAllByMunicipality_Id(municipalityA)));
        assertEquals(List.of("bruno.repo-isolation"), usernames(repository.findAllByMunicipality_Id(municipalityB)));
    }

    //passa, guardar com o município A cria um utilizador INACTIVE visível apenas em A
    @Test
    void saveCreatesInactiveUserInRepositoryMunicipality() {
        long municipalityA = municipality(jdbc, "repo-save-a");
        long municipalityB = municipality(jdbc, "repo-save-b");
        long teamA = team(jdbc, municipalityA, "parks-save-a");
        Municipality municipality = entityManager.find(Municipality.class, municipalityA);
        Team team = entityManager.find(Team.class, teamA);
        AppUser user = new AppUser(municipality, team, "Carla", "carla.repo-save", "hash", AppUserRole.FIELD_WORKER);

        AppUser saved = repository.save(municipalityA, user);
        entityManager.flush();

        assertEquals(AppUserStatus.INACTIVE, saved.getStatus());
        assertEquals("INACTIVE", jdbc.queryForObject(
                "select status from app_user where id = ?", String.class, saved.getId()));
        assertTrue(repository.findByMunicipality_IdAndUsername(municipalityA, "carla.repo-save").isPresent());
        assertTrue(repository.findByMunicipality_IdAndUsername(municipalityB, "carla.repo-save").isEmpty());
    }

    //falha, guardar no contexto A um utilizador de B deve ser recusado com InvalidDataAccessApiUsageException
    @Test
    void saveRejectsUserFromAnotherMunicipality() {
        long municipalityA = municipality(jdbc, "repo-reject-a");
        long municipalityB = municipality(jdbc, "repo-reject-b");
        long teamB = team(jdbc, municipalityB, "parks-reject-b");
        Municipality municipality = entityManager.find(Municipality.class, municipalityB);
        Team team = entityManager.find(Team.class, teamB);
        AppUser user = new AppUser(municipality, team, "Diego", "diego.repo-reject", "hash", AppUserRole.FIELD_WORKER);

        InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> repository.save(municipalityA, user));
        assertTrue(exception.getMostSpecificCause() instanceof IllegalArgumentException);
    }

    //falha, guardar no contexto A um utilizador com time de B deve ser recusado com InvalidDataAccessApiUsageException
    @Test
    void saveRejectsTeamFromAnotherMunicipality() {
        long municipalityA = municipality(jdbc, "repo-team-reject-a");
        long municipalityB = municipality(jdbc, "repo-team-reject-b");
        long teamB = team(jdbc, municipalityB, "parks-team-reject-b");
        Municipality municipality = entityManager.find(Municipality.class, municipalityA);
        Team team = entityManager.find(Team.class, teamB);
        AppUser user = new AppUser(municipality, team, "Eva", "eva.repo-team-reject", "hash",
                AppUserRole.FIELD_WORKER);

        InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> repository.save(municipalityA, user));
        assertTrue(exception.getMostSpecificCause() instanceof IllegalArgumentException);
    }

    //existsByUsername pesquisa globalmente, independentemente do município
    @Test
    void existsByUsernameIsGlobal() {
        long municipalityA = municipality(jdbc, "repo-username-a");
        long municipalityB = municipality(jdbc, "repo-username-b");
        long teamA = team(jdbc, municipalityA, "parks-username-a");
        long teamB = team(jdbc, municipalityB, "parks-username-b");
        user(jdbc, municipalityA, teamA, "Ana", "ana.repo-username", "FIELD_WORKER");
        user(jdbc, municipalityB, teamB, "Bruno", "bruno.repo-username", "FIELD_WORKER");

        assertTrue(repository.existsByUsername("ana.repo-username"));
        assertTrue(repository.existsByUsername("bruno.repo-username"));
        assertFalse(repository.existsByUsername("unknown.repo-username"));
    }

    private List<String> usernames(List<AppUser> users) {
        return users.stream().map(AppUser::getUsername).toList();
    }
}
