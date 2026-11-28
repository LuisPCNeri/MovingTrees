package pt.ua.MovingTree.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.ua.MovingTree.support.IdentityTestData.municipality;
import static pt.ua.MovingTree.support.IdentityTestData.team;
import static pt.ua.MovingTree.support.IdentityTestData.user;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import pt.ua.MovingTree.support.PostgresIntegrationTest;

@SpringBootTest
class IdentitySchemaIT extends PostgresIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    //passa, a migração V1 aplicada a uma base vazia deve criar municipality, team e app_user
    @Test
    void migratesFromEmptyDatabase() {
        assertEquals(1, jdbc.queryForObject("""
                select count(*) from flyway_schema_history
                where version = '1' and success = true
                """, Integer.class));

        Set<String> tables = new HashSet<>(jdbc.queryForList("""
                select table_name from information_schema.tables
                where table_schema = current_schema()
                  and table_name in ('municipality', 'team', 'app_user')
                """, String.class));
        assertEquals(Set.of("municipality", "team", "app_user"), tables);
    }

    //passa, a tabela app_user deve conter todas as colunas exigidas pelo contrato
    @Test
    void appUserHasRequiredColumns() {
        Set<String> columns = new HashSet<>(jdbc.queryForList("""
                select column_name from information_schema.columns
                where table_schema = current_schema() and table_name = 'app_user'
                """, String.class));

        assertTrue(columns.containsAll(Set.of(
                "id", "municipality_id", "team_id", "name", "username", "password_hash",
                "role", "status", "created_at", "updated_at")));
    }

    //passa, devem existir índices em municipality, role, status e username único
    @Test
    void hasRequiredIndexes() {
        Set<String> indexes = new HashSet<>(jdbc.queryForList("""
                select indexname from pg_indexes
                where schemaname = current_schema() and tablename = 'app_user'
                """, String.class));

        assertTrue(indexes.containsAll(Set.of(
                "ix_app_user_municipality",
                "ix_app_user_role",
                "ix_app_user_status",
                "uq_app_user_username")));
    }

    //passa, devem existir as FKs, os checks de role/status e o único de username
    @Test
    void hasRequiredForeignKeysAndChecks() {
        Set<String> appUserConstraints = new HashSet<>(jdbc.queryForList("""
                select conname from pg_constraint
                where conrelid = 'app_user'::regclass
                """, String.class));

        assertTrue(appUserConstraints.containsAll(Set.of(
                "uq_app_user_username",
                "ck_app_user_role",
                "ck_app_user_status",
                "fk_app_user_municipality",
                "fk_app_user_team_municipality")));

        Set<String> teamConstraints = new HashSet<>(jdbc.queryForList("""
                select conname from pg_constraint
                where conrelid = 'team'::regclass
                """, String.class));

        assertTrue(teamConstraints.containsAll(Set.of(
                "fk_team_municipality",
                "uq_team_municipality_name",
                "uq_team_id_municipality")));
    }

    //passa já que sem status explícito, o utilizador é guardado como INACTIVE
    @Test
    void newUserDefaultsToInactive() {
        long municipalityId = municipality(jdbc, "schema-default");
        long teamId = team(jdbc, municipalityId, "Parks and Gardens");
        long userId = user(jdbc, municipalityId, teamId, "Ana", "ana.schema-default", "FIELD_WORKER");

        assertEquals("INACTIVE", jdbc.queryForObject(
                "select status from app_user where id = ?", String.class, userId));
    }
}
