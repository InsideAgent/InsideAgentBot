package unit.mocks;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;
import org.mockito.MockingDetails;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class JDAMock {
    private static final List<Command> commandList = new ArrayList<>();


    public static JDA getJDA() {
        return getJDA("UnitTesting", "1111");
    }

    @NotNull
    public static JDA getJDA(String name, String discriminator) {
        try {
            JDA jda = Mockito.mock(JDAImpl.class);
            Mockito.when(jda.getStatus()).thenAnswer(invocation -> JDA.Status.CONNECTED);
            Mockito.when(jda.unloadUser(Mockito.anyLong())).thenAnswer(invocation -> true);
            Mockito.when(jda.awaitReady()).thenAnswer(invocationOnMock -> jda);
            Mockito.when(jda.awaitStatus(Mockito.any(JDA.Status.class))).thenAnswer(invocationOnMock -> jda);
            Mockito.when(jda.awaitStatus(Mockito.any(JDA.Status.class), Mockito.any(JDA.Status[].class))).thenAnswer(invocationOnMock -> jda);
            Mockito.when(jda.getSelfUser()).thenAnswer(invocationOnMock -> getSelfUser(name, discriminator));
            Mockito.when(jda.updateCommands()).thenAnswer(invocationOnMock -> getCommandUpdate());

            return jda;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static CommandListUpdateAction getCommandUpdate() {
        CommandListUpdateAction update = Mockito.mock(CommandListUpdateAction.class);

        Mockito.when(update.addCommands(Mockito.anyList())).thenAnswer(invocationOnMock -> {
            commandList.addAll(invocationOnMock.getArgument(0));
            return update;
        });


        Mockito.doAnswer(invocationOnMock -> update).when(update).queue();

        return update;
    }


    public static SelfUser getSelfUser(String name, String discriminator) {
        SelfUser selfUser = Mockito.mock(SelfUser.class);

        Mockito.when(selfUser.getName()).thenAnswer(invocationOnMock -> name);
        Mockito.when(selfUser.getId()).thenAnswer(invocationOnMock -> "0");
        Mockito.when(selfUser.getIdLong()).thenAnswer(invocationOnMock -> 0L);

        return selfUser;
    }


    public static Member getMember(String name, String discriminator) {
        Member member = Mockito.mock(Member.class);

        Mockito.when(member.getEffectiveName()).thenAnswer(invocationOnMock -> name);
        Mockito.when(member.getNickname()).thenAnswer(invocationOnMock -> name);

        User user = Mockito.mock(User.class);
        Mockito.when(user.getName()).thenAnswer(invocationOnMock -> name);

        Mockito.when(member.getUser()).thenAnswer(invocationOnMock -> user);

        return member;

    }

    public static List<Command> getCommandList() {
        return commandList;
    }
}
