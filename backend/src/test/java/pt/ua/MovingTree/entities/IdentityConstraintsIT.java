package pt.ua.MovingTree.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.ua.MovingTree.support.IdentityTestData.municipality;
import static pt.ua.MovingTree.support.IdentityTestData.team;
import static pt.ua.MovingTree.support.IdentityTestData.user;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import pt.ua.MovingTree.support.PostgresIntegrationTest;

@SpringBootTest
class IdentityConstraintsIT extends PostgresIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    //falha ao repetir o username noutro município deve violar UNIQUE com SQLState 23505
    @Test
    void duplicateUsernameIsRejectedAcrossMunicipalities() {
        long municipalityA = municipality(jdbc, "constraints-username-a");
        long municipalityB = municipality(jdbc, "constraints-username-b");
        long teamA = team(jdbc, municipalityA, "team-a");
        long teamB = team(jdbc, municipalityB, "team-b");
        user(jdbc, municipalityA, teamA, "Ana", "duplicate.constraints.username", "FIELD_WORKER");

        assertSqlState("23505", () -> user(jdbc, municipalityB, teamB, "Bruno", "duplicate.constraints.username",
                "FIELD_WORKER"));
    }

    //falha, o role GARDENER está fora do CHECK e deve gerar SQLState 23514
    @Test
    void invalidRoleIsRejected() {
        long municipalityId = municipality(jdbc, "constraints-role");
        long teamId = team(jdbc, municipalityId, "team-role");

        assertSqlState("23514", () -> user(jdbc, municipalityId, teamId, "Ana", "invalid.role", "GARDENER"));
    }

    //falha, o status SUSPENDED está fora do CHECK e deve gerar SQLState 23514
    @Test
    void invalidStatusIsRejected() {
        long municipalityId = municipality(jdbc, "constraints-status");
        long teamId = team(jdbc, municipalityId, "team-status");

        assertSqlState("23514", () -> user(jdbc, municipalityId, teamId, "Ana", "invalid.status",
                "FIELD_WORKER", "SUSPENDED"));
    }

    //falha, um municipality_id inexistente deve violar a FK com SQLState 23503
    @Test
    void invalidMunicipalityReferenceIsRejected() {
        assertSqlState("23503", () -> jdbc.update(
                "insert into team (municipality_id, name) values (?, ?)", 999999L, "orphan-team"));
    }

    //falha, um team_id inexistente deve violar a FK composta com SQLState 23503
    @Test
    void invalidTeamReferenceIsRejected() {
        long municipalityId = municipality(jdbc, "constraints-team-fk");

        assertSqlState("23503", () -> user(jdbc, municipalityId, 999999L, "Ana", "invalid.team.fk",
                "FIELD_WORKER"));
    }

    //falha já que um team de outro município deve violar a FK composta com SQLState 23503
    @Test
    void teamFromAnotherMunicipalityIsRejected() {
        long municipalityA = municipality(jdbc, "constraints-team-a");
        long municipalityB = municipality(jdbc, "constraints-team-b");
        long teamA = team(jdbc, municipalityA, "foreign-team");

        assertSqlState("23503", () -> user(jdbc, municipalityB, teamA, "Ana", "foreign.team.user",
                "FIELD_WORKER"));
    }

    //falha, eliminar municipality/team referenciados deve violar a FK com SQLState 23503
    @Test
    void referencedMunicipalityAndTeamCannotBeDeleted() {
        long municipalityId = municipality(jdbc, "constraints-delete");
        long teamId = team(jdbc, municipalityId, "team-delete");
        user(jdbc, municipalityId, teamId, "Ana", "delete.constraints.user", "FIELD_WORKER");

        assertSqlState("23503", () -> jdbc.update("delete from team where id = ?", teamId));
        assertSqlState("23503", () -> jdbc.update("delete from municipality where id = ?", municipalityId));
    }

    private void assertSqlState(String expectedSqlState, Executable operation) {
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, operation);
        Throwable cause = exception.getMostSpecificCause();
        assertTrue(cause instanceof SQLException, () -> "Expected SQLException but got " + cause);
        assertEquals(expectedSqlState, ((SQLException) cause).getSQLState());
    }
}
