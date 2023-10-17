package unit.mocks;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.mockito.Mockito;

import java.util.EnumSet;

public class MockRole {

    public static Role mockRole(EnumSet<Permission> permissions) {
        Role role = Mockito.mock(Role.class);

        Mockito.when(role.getPermissionsRaw()).thenAnswer(invocationOnMock ->  Permission.getRaw(permissions));

        return role;
    }
}
