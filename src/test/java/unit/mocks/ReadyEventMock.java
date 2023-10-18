package unit.mocks;

import dev.jacrispys.JavaBot.events.BotStartup;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionState;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static unit.mocks.JDAMock.getJDA;

public class ReadyEventMock {


    public static BotStartup mockBotStartup() {
        BotStartup startup = Mockito.spy(BotStartup.class);

        Mockito.doReturn(BypassDb.mockSqlConnection()).when(startup).getConnection();
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



    public static List<CommandData> testReadyEventCommands(EventListener listener) {
        ReadyEvent event = getReadyEventCommands();

        listener.onEvent(event);
        return JDAMock.getCommandList();
    }

    public static void assertUpdateCommands(EventListener listener, List<String> expectedOutput) {
        List<CommandData> cmds = testReadyEventCommands(listener);
        List<String> actual = new ArrayList<>(cmds.stream().map(CommandData::getName).toList());

        Collections.sort(actual);
        Assertions.assertEquals(expectedOutput, actual);
    }

    public static SessionState testReadyStatus(EventListener listener) {
        ReadyEvent event = getReadyEvent();

        listener.onEvent(event);
        return event.getState();
    }

    public static void assertReadyStatus(EventListener listener) {
        Assertions.assertEquals(SessionState.READY, testReadyStatus(listener));
    }
}
