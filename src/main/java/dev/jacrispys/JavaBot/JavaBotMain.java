package dev.jacrispys.JavaBot;

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.jacrispys.JavaBot.api.analytics.utils.ListenTimeTracker;
import dev.jacrispys.JavaBot.api.libs.utils.JavalinManager;
import dev.jacrispys.JavaBot.audio.AudioPlayerButtons;
import dev.jacrispys.JavaBot.audio.GenerateGenrePlaylist;
import dev.jacrispys.JavaBot.audio.InactivityTimer;
import dev.jacrispys.JavaBot.commands.ComplaintCommand;
import dev.jacrispys.JavaBot.commands.EmbedCLI;
import dev.jacrispys.JavaBot.commands.RegisterGuildCommand;
import dev.jacrispys.JavaBot.commands.UnclassifiedSlashCommands;
import dev.jacrispys.JavaBot.commands.audio.GenericMusicCommands;
import dev.jacrispys.JavaBot.commands.audio.SlashMusicCommands;
import dev.jacrispys.JavaBot.commands.debug.GenericDebugCommands;
import dev.jacrispys.JavaBot.commands.private_message.DefaultPrivateMessageResponse;
import dev.jacrispys.JavaBot.events.BotStartup;
import dev.jacrispys.JavaBot.utils.SecretData;
import dev.jacrispys.JavaBot.utils.SpotifyManager;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;


public class JavaBotMain {

    private static final Logger logger = LoggerFactory.getLogger(JavaBotMain.class);
    private static final String className = JavaBotMain.class.getSimpleName();
    public static AudioPlayerManager audioManager;

    public static void main(String[] args) throws Exception {
        logger.info("{} - Installing & loading data Files.", className);
        SecretData.initLoginInfo();
        String devToken = SecretData.getToken(true);
        String botToken = SecretData.getToken();

        if(devToken == null || botToken == null) {
            throw new ConfigurationException("Config file MUST contain VALID values for all fields!");
        }

        logger.info("{} - Logging into bot & discord servers...", className);
        JDA jda = JDABuilder.createDefault(botToken)
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

        String version = JavaBotMain.class.getPackage().getImplementationVersion();

        jda.getPresence().setActivity(Activity.streaming(version + " Woo!", "https://www.twitch.tv/jacrispyslive"));
        logger.info("{} - Starting event listeners...", className);
        jda.addEventListener(new SlashMusicCommands());
        jda.addEventListener(new DefaultPrivateMessageResponse());
        jda.addEventListener(new ComplaintCommand());
        jda.addEventListener(new RegisterGuildCommand());
        jda.addEventListener(new BotStartup());
        jda.addEventListener(new GenericMusicCommands());
        jda.addEventListener(new AudioPlayerButtons());
        jda.addEventListener(new InactivityTimer());
        jda.addEventListener(new GenericDebugCommands());
        jda.addEventListener(new UnclassifiedSlashCommands(jda));
        jda.addEventListener(EmbedCLI.getInstance());
        jda.addEventListener(new GenerateGenrePlaylist());
        jda.addEventListener(new ListenTimeTracker(jda));
        logger.info("{} - Successfully added [" + jda.getRegisteredListeners().size() + "] event listeners!", className);


        new JavalinManager(7070);


    }

}
