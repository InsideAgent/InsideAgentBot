package dev.jacrispys.JavaBot;

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.jacrispys.JavaBot.Audio.AudioPlayerButtons;
import dev.jacrispys.JavaBot.Audio.GenerateGenrePlaylist;
import dev.jacrispys.JavaBot.Audio.InactivityTimer;
import dev.jacrispys.JavaBot.Commands.Audio.GenericMusicCommands;
import dev.jacrispys.JavaBot.Commands.Audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.Commands.*;
import dev.jacrispys.JavaBot.Commands.PrivateMessageCommands.DefaultPrivateMessageResponse;
import dev.jacrispys.JavaBot.Commands.RuntimeDebug.GenericDebugCommands;
import dev.jacrispys.JavaBot.Events.BotStartup;
import dev.jacrispys.JavaBot.Utils.GameSpyThread;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import dev.jacrispys.JavaBot.Utils.SecretData;
import dev.jacrispys.JavaBot.Utils.SpotifyManager;
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

import javax.naming.ConfigurationException;


public class JavaBotMain {

    private static GameSpyThread gameSpyThread;
    private static final Logger logger = LoggerFactory.getLogger(JavaBotMain.class);
    private static final String className = JavaBotMain.class.getSimpleName();
    public static AudioPlayerManager audioManager;

    public static void main(String[] args) throws Exception {


        AnsiConsole.systemInstall();
        logger.info("{} - Jansi Installed.", className);

        logger.info("{} - Installing & loading data Files.", className);
        SecretData.initLoginInfo();
        String devToken = SecretData.getToken(true);
        String botToken = SecretData.getToken();

        if(devToken == null || botToken == null) {
            throw new ConfigurationException("Config file MUST contain VALID values for all fields!");
        }

        logger.info("{} - Logging into bot & discord servers...", className);
        JDA jda = JDABuilder.createDefault(devToken)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE)
                .build();
        logger.info("{} - Login Successful!", className);

        logger.info("{} - Connecting to MySQL Database...", className);
        MySQLConnection mySQLConnection = new MySQLConnection();
        mySQLConnection.getConnection("inside_agent_bot");
        logger.info("{} - DB-Connection Successful!", className);

        logger.info("{} - Connecting to spotify source manager...", className);
        audioManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioManager);
        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(SecretData.getSpotifyId());
        spotifyConfig.setClientSecret(SecretData.getSpotifySecret());
        spotifyConfig.setCountryCode("US");
        audioManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, audioManager));
        AudioSourceManagers.registerRemoteSources(audioManager);
        logger.info("{} - Successfully connected to spotify!", className);

        logger.info("{} - Connecting to personal spotify API...", className);
        SpotifyManager.getInstance();
        logger.info("{} - Connected to personal API!", className);

        jda.getPresence().setActivity(Activity.streaming("Version-0.1.8 Woo!", "https://www.twitch.tv/jacrispyslive"));
        logger.info("{} - Starting event listeners...", className);
        jda.addEventListener(new SlashMusicCommands());
        jda.addEventListener(new DefaultPrivateMessageResponse());
        jda.addEventListener(new ComplaintCommand());
        jda.addEventListener(new RegisterGuildCommand());
        jda.addEventListener(new GameSpyCommand());
        jda.addEventListener(new BotStartup());
        jda.addEventListener(new GenericMusicCommands());
        jda.addEventListener(new AudioPlayerButtons());
        jda.addEventListener(new InactivityTimer());
        jda.addEventListener(new GenericDebugCommands());
        jda.addEventListener(new UnclassifiedSlashCommands());
        jda.addEventListener(EmbedCLI.getInstance());
        jda.addEventListener(new GenerateGenrePlaylist());
        logger.info("{} - Successfully added [" + jda.getRegisteredListeners().size() + "] event listeners!", className);
        logger.info("{} - Starting GameSpyThread...", className);
        gameSpyThread = new GameSpyThread(jda);
        gameSpyThread.start();

        logger.info("{} - Enabling command line interface...", className);


    }

    public static GameSpyThread getGameSpyThread() {
        return gameSpyThread;
    }
}
