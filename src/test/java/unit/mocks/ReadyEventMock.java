package unit.mocks;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionState;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import static unit.mocks.JDAMock.getJDA;

public class ReadyEventMock {

    public static ReadyEvent getReadyEvent() {
        ReadyEvent event = Mockito.mock(ReadyEvent.class);

        Mockito.when(event.getState()).thenAnswer(invocationOnMock -> SessionState.READY);
        Mockito.when(event.getJDA()).thenAnswer(invocationOnMock -> getJDA());
        Mockito.when(event.getGuildTotalCount()).thenAnswer(invocationOnMock -> 0);

        return event;
    }

    public static void testCommandRegister(EventListener listener, List<Command> commands) {
        ReadyEvent event = getReadyEvent();
    }
}
