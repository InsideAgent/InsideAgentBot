package dev.jacrispys.JavaBot;

import dev.jacrispys.JavaBot.Audio.AudioPlayerButtons;
import dev.jacrispys.JavaBot.Commands.*;
import dev.jacrispys.JavaBot.Commands.PrivateMessageCommands.DefaultPrivateMessageResponse;
import dev.jacrispys.JavaBot.Events.BotStartup;
import dev.jacrispys.JavaBot.Utils.GameSpyThread;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
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
        JDA jda = JDABuilder.createDefault(System.getenv("TOKEN"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE)
                .build();


        MySQLConnection mySQLConnection = new MySQLConnection();
        mySQLConnection.getConnection("inside_agent_bot");

        jda.getPresence().setActivity(Activity.streaming("Version-0.1.5 Woo!", "https://www.twitch.tv/jacrispyslive"));
        jda.addEventListener(new DefaultPrivateMessageResponse());
        jda.addEventListener(new ComplaintCommand());
        jda.addEventListener(new RegisterGuildCommand());
        jda.addEventListener(new GameSpyCommand());
        jda.addEventListener(new BotStartup());
        jda.addEventListener(new MusicCommands());
        jda.addEventListener(new AudioPlayerButtons());
        gameSpyThread = new GameSpyThread(jda);
        gameSpyThread.start();



    }

    public static GameSpyThread getGameSpyThread() {
        return gameSpyThread;
    }
}
