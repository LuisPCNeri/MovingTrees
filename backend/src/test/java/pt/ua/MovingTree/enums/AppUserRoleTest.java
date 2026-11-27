package pt.ua.MovingTree.enums;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class AppUserRoleTest {
    //os papéis do enum devem corresponder exatamente aos valores aceites pelo CHECK
    @Test
    void rolesMatchSchemaCheck() {
        assertArrayEquals(new AppUserRole[] {
            AppUserRole.MUNICIPAL_ADMINISTRATOR,
            AppUserRole.FIELD_WORKER,
            AppUserRole.FLEET_OPERATOR,
            AppUserRole.FIELD_TECHNICIAN,
            AppUserRole.CLIENT_MANAGER,
            AppUserRole.PLATFORM_ADMIN
        }, AppUserRole.values());
    }
}
