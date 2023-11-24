package dev.jacrispys.JavaBot;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
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
import dev.jacrispys.JavaBot.commands.message.DefaultPrivateMessageResponse;
import dev.jacrispys.JavaBot.events.BotStartup;
import dev.jacrispys.JavaBot.utils.SecretData;
import dev.jacrispys.JavaBot.utils.SpotifyManager;
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
import java.io.IOException;


// TODO: 11/22/2023 Stats command + Debug Stats command

/**
 * The core class that establishes a connection with {@link JDA} and discord.
 * <br> Also initializes all other utility/necessary classes.
 */
public class JavaBotMain {

    // TODO: 1/26/2023 Add Documentation to all functions

    private static final Logger logger = LoggerFactory.getLogger(JavaBotMain.class);
    private static final String className = JavaBotMain.class.getSimpleName();
    public static AudioPlayerManager audioManager;

    /**
     * The main method of the application.
     * <br>
     * <br> Starts by initializing ENV login data via {@link SecretData#initLoginInfo()}
     * <br> Then creates a connection to {@link JDA} through the {@link JDABuilder}
     * <br> It next registers Audio source managers for multi-platform song searching through {@link AudioSourceManagers}
     * <br> Finally we add all event listeners and register a {@link io.javalin.Javalin} server to handle API requests (WIP)
     * <br> <br>
     * @throws ConfigurationException if any of the token fields are left blank in the config file
     * @throws IOException if any errors occur whilst obtaining data from the YAML file
     */
    public static void main(String[] args) throws ConfigurationException, IOException {
        logger.info("{} - Installing & loading data Files.", className);
        SecretData.initLoginInfo();
        String devToken = SecretData.getToken(true);
        String botToken = SecretData.getToken();

        if (devToken == null || botToken == null) {
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

        logger.info("{} - Connecting to spotify source manager...", className);
        audioManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioManager);
        String clientId = SecretData.getSpotifyId();
        String clientSecret = SecretData.getSpotifySecret();

        String countryCode = "US";
        YoutubeAudioSourceManager ytSource = new YoutubeAudioSourceManager(true, SecretData.getYtEmail(), SecretData.getYtPass());
        audioManager.registerSourceManager(ytSource);
        audioManager.registerSourceManager(new SpotifySourceManager(null, clientId, clientSecret, countryCode, audioManager));
        audioManager.registerSourceManager(new AppleMusicSourceManager(null, SecretData.getAppleToken(), "us", audioManager));
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
