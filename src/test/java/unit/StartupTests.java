package unit;

import org.junit.jupiter.api.Test;
import unit.mocks.ReadyEventMock;

public class StartupTests {

    @Test
    void commandRegisters() {
        ReadyEventMock.assertUpdateCommands(ReadyEventMock.mockBotStartup(), null);
    }
}
