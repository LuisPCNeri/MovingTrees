package pt.ua.MovingTree.enums;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class AppUserStatusTest {
    //os estados do enum devem corresponder exatamente aos valores aceites pelo CHECK
    @Test
    void statusesMatchSchemaCheck() {
        assertArrayEquals(new AppUserStatus[] {
            AppUserStatus.INACTIVE,
            AppUserStatus.ACTIVE
        }, AppUserStatus.values());
    }
}
