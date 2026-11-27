package pt.ua.MovingTree.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static pt.ua.MovingTree.support.IdentityTestData.municipality;
import static pt.ua.MovingTree.support.IdentityTestData.team;
import static pt.ua.MovingTree.support.IdentityTestData.user;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.PagedResponse;
import pt.ua.MovingTree.dtos.UserFilter;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.security.CallerGuard;
import pt.ua.MovingTree.support.PostgresIntegrationTest;

@SpringBootTest
@Transactional
class UserQueryServiceIT extends PostgresIntegrationTest {
    @Autowired
    private UserQueryService queryService;
    @Autowired
    private JdbcTemplate jdbc;
    @MockitoBean
    private CallerGuard callerGuard;

    //sem filtros, apenas os utilizadores do município da sessão são devolvidos
    @Test
    void returnsOnlyCallerMunicipality() {
        long municipalityA = seedA();
        long municipalityB = municipality(jdbc, "query-b");
        long teamB = team(jdbc, municipalityB, "parks-query-b");
        user(jdbc, municipalityB, teamB, "Daniel", "daniel.query", "FIELD_WORKER", "ACTIVE");
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(municipalityA);

        PagedResponse<UserResponse> response = queryService.list(
                0, 20, new UserFilter(null, null, null, null));

        assertEquals(List.of("ana.query", "bruno.query", "carla.query"), usernames(response));
        assertTrue(response.content().stream().allMatch(user -> user.municipalityId().equals(municipalityA)));
        assertEquals(3, response.totalElements());
    }

    //role, team, status e name search combinam-se com AND dentro do município da sessão
    @Test
    void combinesFiltersWithinCallerMunicipality() {
        long municipalityA = seedA();
        long parksA = teamId(municipalityA, "parks-query-a");
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(municipalityA);

        PagedResponse<UserResponse> response = queryService.list(0, 20, new UserFilter(
                AppUserRole.FIELD_WORKER, parksA, AppUserStatus.ACTIVE, "an"));

        assertEquals(List.of("ana.query"), usernames(response));
        assertEquals(1, response.totalElements());
    }

    private long seedA() {
        long municipalityA = municipality(jdbc, "query-a");
        long parksA = team(jdbc, municipalityA, "parks-query-a");
        long opsA = team(jdbc, municipalityA, "ops-query-a");
        user(jdbc, municipalityA, parksA, "Ana Silva", "ana.query", "FIELD_WORKER", "ACTIVE");
        user(jdbc, municipalityA, parksA, "Bruno Costa", "bruno.query", "FIELD_TECHNICIAN", "INACTIVE");
        user(jdbc, municipalityA, opsA, "Carla Dias", "carla.query", "FIELD_WORKER", "ACTIVE");
        return municipalityA;
    }

    private long teamId(long municipalityId, String name) {
        return jdbc.queryForObject("select id from team where municipality_id = ? and name = ?",
                Long.class, municipalityId, name);
    }

    private List<String> usernames(PagedResponse<UserResponse> response) {
        return response.content().stream().map(UserResponse::username).toList();
    }
}
