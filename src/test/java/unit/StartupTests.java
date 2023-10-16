package unit;

import dev.jacrispys.JavaBot.events.BotStartup;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.junit.jupiter.api.Test;
import unit.mocks.ReadyEventMock;

import java.util.EventListener;

public class StartupTests {

    @Test
    void commandRegisters() {
        ReadyEventMock.assertUpdateCommands(ReadyEventMock.startClass(), null);
    }
}
