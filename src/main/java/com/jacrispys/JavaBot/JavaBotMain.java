package com.jacrispys.JavaBot;

import com.jacrispys.JavaBot.Commands.ComplaintCommand;
import com.jacrispys.JavaBot.Commands.GameSpyCommand;
import com.jacrispys.JavaBot.Commands.PrivateMessageCommands.DefaultPrivateMessageResponse;
import com.jacrispys.JavaBot.Commands.RegisterGuildCommand;
import com.jacrispys.JavaBot.Events.BotStartup;
import com.jacrispys.JavaBot.Utils.GameSpyThread;
import com.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;


public class JavaBotMain {

    private static GameSpyThread gameSpyThread;

    public static void main(String[] args) throws Exception {
        JDA jda = JDABuilder.createDefault("Nzg2NzIxNzU1NTYwODA0Mzcz.X9Khuw.Y0pgvYATjsNpAKzRMwEeXPnGsi8")
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY)
                .build();

        MySQLConnection mySQLConnection = new MySQLConnection();
        mySQLConnection.getConnection("inside_agent_bot");

        jda.getPresence().setActivity(Activity.streaming("Well this is a thing now...", "https://www.twitch.tv/jacrispyslive"));
        jda.addEventListener(new DefaultPrivateMessageResponse());
        jda.addEventListener(new ComplaintCommand());
        jda.addEventListener(new RegisterGuildCommand());
        jda.addEventListener(new GameSpyCommand());
        jda.addEventListener(new BotStartup());
        gameSpyThread = new GameSpyThread(jda);
        gameSpyThread.start();


    }

    public static GameSpyThread getGameSpyThread() {
        return gameSpyThread;
    }
}
