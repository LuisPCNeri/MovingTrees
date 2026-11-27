package pt.ua.MovingTree.support;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public final class IdentityTestData {
    private IdentityTestData() {
    }

    public static long municipality(JdbcTemplate jdbc, String name) {
        return insert(jdbc, "insert into municipality (name) values (?)", name);
    }

    public static long team(JdbcTemplate jdbc, long municipalityId, String name) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into team (municipality_id, name) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, municipalityId);
            statement.setString(2, name);
            return statement;
        }, keyHolder);
        return key(keyHolder);
    }

    public static long user(JdbcTemplate jdbc, long municipalityId, long teamId, String name, String username, String role) {
        return user(jdbc, municipalityId, teamId, name, username, role, null);
    }

    public static long user(JdbcTemplate jdbc, long municipalityId, long teamId, String name, String username, String role,
            String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = status == null
                    ? connection.prepareStatement("""
                            insert into app_user (municipality_id, team_id, name, username, password_hash, role)
                            values (?, ?, ?, ?, ?, ?)
                            """, Statement.RETURN_GENERATED_KEYS)
                    : connection.prepareStatement("""
                            insert into app_user (municipality_id, team_id, name, username, password_hash, role, status)
                            values (?, ?, ?, ?, ?, ?, ?)
                            """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, municipalityId);
            statement.setLong(2, teamId);
            statement.setString(3, name);
            statement.setString(4, username);
            statement.setString(5, "hashed-password");
            statement.setString(6, role);
            if (status != null) {
                statement.setString(7, status);
            }
            return statement;
        }, keyHolder);
        return key(keyHolder);
    }

    private static long insert(JdbcTemplate jdbc, String sql, String value) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, value);
            return statement;
        }, keyHolder);
        return key(keyHolder);
    }

    private static long key(KeyHolder keyHolder) {
        return ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("id")).longValue();
    }
}
