package dev.jacrispys.JavaBot.api.libs.auth;

import dev.jacrispys.JavaBot.utils.SecretData;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

/**
 * General connection obtained through {@link dev.jacrispys.JavaBot.api.libs.AgentApi}
 * <br> Should not be instantiated!
 * @see DeveloperConnection
 * @see UserConnection
 */
public abstract class ClientConnection {

    private final JDA jda;

    protected ClientConnection() throws InterruptedException {
        this.jda = JDABuilder.createDefault(SecretData.getToken())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE)
                .build();
        jda.awaitReady();

    }

    /**
     * @return instance of the JDA API
     */
    public JDA getJDA() {
        return jda;
    }
}
