package unit;

import dev.jacrispys.JavaBot.commands.UnclassifiedSlashCommands;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import unit.mocks.JDAMock;
import unit.mocks.SlashCommandMock;

import java.util.List;

public class UnclassifiedCommandsTests {

    @Test
    void checkPermSetNick() throws InterruptedException {
        Member member = JDAMock.getMember("Jacrispy", List.of(Permission.ADMINISTRATOR));
        String name = "setnick";
        Message m = SlashCommandMock.testSlashCommandReply(new UnclassifiedSlashCommands(JDAMock.getJDA()), name, member);
        String actual = m.getContentRaw();
        String expected = "You do not have permission to use this command!";

        Assertions.assertEquals(expected, actual);
    }

}
