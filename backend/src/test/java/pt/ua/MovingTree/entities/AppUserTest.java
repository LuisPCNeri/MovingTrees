package pt.ua.MovingTree.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;

class AppUserTest {
    //pass com parâmetros válidos, o construtor cria o utilizador INACTIVE com os dados fornecidos
    @Test
    void constructorCreatesInactiveUser() {
        Municipality municipality = new Municipality("Aveiro");
        Team team = new Team(municipality, "Parks and Gardens");

        AppUser user = new AppUser(municipality, team, "Paulo", "paulo", "hash", AppUserRole.FIELD_WORKER);

        assertNull(user.getId());
        assertSame(municipality, user.getMunicipality());
        assertSame(team, user.getTeam());
        assertEquals("Paulo", user.getName());
        assertEquals("paulo", user.getLogin());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(AppUserRole.FIELD_WORKER, user.getRole());
        assertEquals(AppUserStatus.INACTIVE, user.getStatus());
    }
}
