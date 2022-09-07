package dev.jacrispys.JavaBot.api.libs;

import dev.jacrispys.JavaBot.api.libs.auth.ClientConnection;
import dev.jacrispys.JavaBot.api.libs.auth.DeveloperConnection;
import dev.jacrispys.JavaBot.api.libs.auth.UserConnection;

public interface AgentApi {

    UserConnection getUserConnection();
    DeveloperConnection getDevConnection();

    ClientConnection getConnection();

    // TODO: 8/31/2022 Add Methods to check data and perform changes on the bot. 
}
