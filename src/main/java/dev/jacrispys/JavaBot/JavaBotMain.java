package dev.jacrispys.JavaBot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import dev.jacrispys.JavaBot.Audio.AudioPlayerButtons;
import dev.jacrispys.JavaBot.Audio.InactivityTimer;
import dev.jacrispys.JavaBot.Commands.ComplaintCommand;
import dev.jacrispys.JavaBot.Commands.GameSpyCommand;
import dev.jacrispys.JavaBot.Commands.PrivateMessageCommands.DefaultPrivateMessageResponse;
import dev.jacrispys.JavaBot.Commands.RegisterGuildCommand;
import dev.jacrispys.JavaBot.Commands.RuntimeDebug.DebugCommands;
import dev.jacrispys.JavaBot.Events.BotStartup;
import dev.jacrispys.JavaBot.Utils.GameSpyThread;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import dev.jacrispys.JavaBot.Utils.SecretData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JavaBotMain {

    private static GameSpyThread gameSpyThread;
    private static final Logger logger = LoggerFactory.getLogger(JavaBotMain.class);
    private static final String className = JavaBotMain.class.getSimpleName();
    public static AudioPlayerManager audioManager;

    private static final String botToken = SecretData.getToken();

    public static void main(String[] args) throws Exception {

        AnsiConsole.systemInstall();
        logger.info("{} - Jansi Installed.", className);

        logger.info("{} - Logging into bot & discord servers...", className);
        JDA jda = JDABuilder.createDefault(botToken)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE)
                .build();
        logger.info("{} - Login Successful!", className);

        logger.info("{} - Connecting to MySQL Database...", className);
        MySQLConnection mySQLConnection = new MySQLConnection();
        mySQLConnection.getConnection("inside_agent_bot");
        logger.info("{} - DB-Connection Successful!", className);

        audioManager = new DefaultAudioPlayerManager();

        jda.getPresence().setActivity(Activity.streaming("Version-0.1.6 Woo!", "https://www.twitch.tv/jacrispyslive"));
        logger.info("{} - Starting event listeners...", className);
        jda.addEventListener(new DefaultPrivateMessageResponse());
        jda.addEventListener(new ComplaintCommand());
        jda.addEventListener(new RegisterGuildCommand());
        jda.addEventListener(new GameSpyCommand());
        jda.addEventListener(new BotStartup());
        jda.addEventListener(new AudioPlayerButtons());
        jda.addEventListener(new InactivityTimer());
        jda.addEventListener(new DebugCommands());
        logger.info("{} - Successfully added [" + jda.getRegisteredListeners().size() + "] event listeners!", className);
        logger.info("{} - Starting GameSpyThread...", className);
        gameSpyThread = new GameSpyThread(jda);
        gameSpyThread.start();


    }

    public static GameSpyThread getGameSpyThread() {
        return gameSpyThread;
    }
}
