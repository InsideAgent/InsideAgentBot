package unit;

import org.junit.jupiter.api.Test;
import unit.mocks.ReadyEventMock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StartupTests {

    @Test
    void commandRegisters() {
        List<String> expected = new ArrayList<>();
        expected.add("setnick");
        expected.add("embedbuilder");
        expected.add("auth-token");

        expected.add("play");
        expected.add("skip");
        expected.add("volume");
        expected.add("clear");
        expected.add("stop");
        expected.add("pause");
        expected.add("resume");
        expected.add("dc");
        expected.add("leave");
        expected.add("disconnect");
        expected.add("follow");
        expected.add("queue");
        expected.add("shuffle");
        expected.add("song");
        expected.add("song-info");
        expected.add("info");
        expected.add("remove");
        expected.add("seek");
        expected.add("fix");
        expected.add("loop");
        expected.add("move");
        expected.add("hijack");
        expected.add("playtop");
        expected.add("skipto");
        expected.add("fileplay");
        expected.add("radio");

        Collections.sort(expected);

        ReadyEventMock.assertUpdateCommands(ReadyEventMock.mockBotStartup(), expected);
    }

    @Test
    void readyStatus() {
        ReadyEventMock.testReadyStatus(ReadyEventMock.mockBotStartup());
    }
}
