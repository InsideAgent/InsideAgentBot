package unit.mocks;

import dev.jacrispys.JavaBot.events.BotStartup;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionState;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockingDetails;
import org.mockito.Mockito;

import java.util.List;

import static unit.mocks.JDAMock.getJDA;

public class ReadyEventMock extends BotStartup {

    public ReadyEventMock() {

    }


    public static ReadyEventMock mockBotStartup() {
        ReadyEventMock startup = Mockito.spy(ReadyEventMock.class);

        Mockito.when(startup.getConnection()).thenAnswer(invocationOnMock ->  BypassDb.mockSqlConnection());
        Mockito.doCallRealMethod().when(startup).onReady(Mockito.any());
        Mockito.doCallRealMethod().when(startup).onGuildJoin(Mockito.any());

        return startup;
    }


    public static ReadyEvent getReadyEvent() {
        ReadyEvent event = Mockito.mock(ReadyEvent.class);

        Mockito.when(event.getState()).thenAnswer(invocationOnMock -> SessionState.READY);
        Mockito.when(event.getJDA()).thenAnswer(invocationOnMock -> getJDA());
        Mockito.when(event.getGuildTotalCount()).thenAnswer(invocationOnMock -> 0);

        return event;
    }

    public static ReadyEvent getReadyEventCommands() {
        return getReadyEvent();
    }



    public static List<Command> testReadyEventCommands(EventListener listener) {
        ReadyEvent event = getReadyEventCommands();

        listener.onEvent(event);
        return JDAMock.getCommandList();
    }

    public static void assertUpdateCommands(EventListener listener, List<Command> expectedOutput) {
        Assertions.assertEquals(expectedOutput, testReadyEventCommands(listener));
    }
}
