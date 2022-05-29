package dev.jacrispys.JavaBot.Audio;

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import dev.jacrispys.JavaBot.Utils.SecretData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import static dev.jacrispys.JavaBot.JavaBotMain.audioManager;

public class GuildAudioManager {
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final Map<AudioTrack, User> requester = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GuildAudioManager.class);
    private static final String className = GuildAudioManager.class.getSimpleName();

    private boolean djEnabled = false;

    private static final Map<Guild, GuildAudioManager> audioManagers = new HashMap<>();

    private static Guild currentGuild = null;

    /**
     * @param guild is the instance to retrieve
     * @return instance of {@link GuildAudioManager}
     */
    public static synchronized GuildAudioManager getGuildAudioManager(Guild guild) {
        if (audioManagers.get(guild) == null) {
            GuildAudioManager audioManager = new GuildAudioManager(guild);
            logger.info("{} - Creating new GuildAudioManager for [" + guild.getName() + "]", className);
            audioManagers.put(guild, audioManager);
            if (currentGuild == null) currentGuild = guild;
            return audioManager;
        }
        if (currentGuild == null) currentGuild = guild;
        return audioManagers.get(guild);
    }


    protected GuildAudioManager(Guild instance) {
        this.audioPlayer = audioManager.createPlayer();
        AudioSourceManagers.registerLocalSource(audioManager);

        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(SecretData.getSpotifyId());
        spotifyConfig.setClientSecret(SecretData.getSpotifySecret());
        spotifyConfig.setCountryCode("US");
        audioManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, audioManager));

        currentGuild = instance;

        AudioSourceManagers.registerRemoteSources(audioManager);
        this.scheduler = new TrackScheduler(this.audioPlayer, currentGuild);
        audioPlayer.addListener(this.scheduler);
        sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        YoutubeHttpContextFilter.setPSID(SecretData.getPSID());
        YoutubeHttpContextFilter.setPAPISID(SecretData.getPAPISID());
        logger.info("{} - Successfully added GuildAudioManager for [" + currentGuild.getName() + "]", className);
    }

    /**
     * @return instance of AudioManager
     */
    public AudioPlayerManager getAudioManager() {
        return audioManager;
    }

    public static Map<Guild, GuildAudioManager> getAudioManagers() {
        return Collections.unmodifiableMap(audioManagers);
    }

    /**
     * @param trackUrl     of the loaded track
     * @param track        instance of loaded track
     * @param voiceChannel to attach bot to
     * @param playTop      is {@link Boolean} for whether the loaded track should be placed at the top of the queue
     */
    public MessageEmbed trackLoaded(String trackUrl, AudioTrack track, VoiceChannel voiceChannel, boolean playTop) {
        play(voiceChannel.getGuild(), getGuildAudioManager(voiceChannel.getGuild()), track, voiceChannel, playTop);
        return djEnabled ? djEnabledEmbed(voiceChannel.getJDA()) : songLoadedMessage(trackUrl, track);
    }

    /**
     * @param trackUrl     of the loaded track
     * @param playlist     instance of loaded playlist
     * @param voiceChannel to attach bot to
     * @param playTop      is {@link Boolean} for whether the loaded track should be placed at the top of the queue
     */
    public MessageEmbed playListLoaded(String trackUrl, AudioPlaylist playlist, VoiceChannel voiceChannel, boolean playTop) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }


        play(voiceChannel.getGuild(), getGuildAudioManager(voiceChannel.getGuild()), firstTrack, voiceChannel, playTop);

        if (!playlist.isSearchResult()) {
            for (int i = 1; i < playlist.getTracks().size(); i++) {
                play(voiceChannel.getGuild(), getGuildAudioManager(voiceChannel.getGuild()), playlist.getTracks().get(i), voiceChannel, false);
            }
            return djEnabled ? djEnabledEmbed(voiceChannel.getJDA()) : playlistLoadedMessage(trackUrl, playlist, false);
        } else {
            return djEnabled ? djEnabledEmbed(voiceChannel.getJDA()) : playlistLoadedMessage(trackUrl, playlist, true);
        }
    }

    /**
     * @param jda to get self instance
     */
    private MessageEmbed djEnabledEmbed(JDA jda) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.decode("e03131"));
        embedBuilder.setAuthor( "Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬", null, jda.getSelfUser().getEffectiveAvatarUrl());
        return embedBuilder.build();
    }

    /**
     * @param trackUrl of loaded song
     * @param track    instance of loaded track
     * @return a {@link MessageEmbed} to be sent and managed by {@link GuildAudioManager#trackLoaded(String, AudioTrack, VoiceChannel, boolean)} }
     */
    private MessageEmbed songLoadedMessage(String trackUrl, AudioTrack track) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Adding song to queue...");
        embedBuilder.addField("Title: ", "`" + track.getInfo().title + "`", false);
        embedBuilder.addField("Author: ", "`" + track.getInfo().author + "`", false);
        embedBuilder.addField("Link: ", trackUrl, false);
        embedBuilder.addField("Position in queue: ", "`" + scheduler.getTrackQueue().size() + "`", false);
        long rawTimeUntilPlay = 0;
        for (AudioTrack queue : scheduler.getTrackQueue().stream().toList()) {
            rawTimeUntilPlay += queue.getDuration();
        }
        rawTimeUntilPlay -= track.getDuration();
        if (audioPlayer.getPlayingTrack() != null) {
            rawTimeUntilPlay += (audioPlayer.getPlayingTrack().getDuration() - audioPlayer.getPlayingTrack().getPosition());
        }
        if (rawTimeUntilPlay < 0) {
            rawTimeUntilPlay = 0;
        }
        String timeUntilPlay = DurationFormatUtils.formatDuration(rawTimeUntilPlay, "HH:mm:ss");
        embedBuilder.addField("Estimated time until track plays: ", "`" + timeUntilPlay + "`", false);
        embedBuilder.setFooter("From Playlist: ❌");
        embedBuilder.setColor(Color.decode("#34d2eb"));
        return embedBuilder.build();

    }

    /**
     * @param trackUrl   of loaded playlist
     * @param playlist   instance of AudioPlaylist
     * @param singleSong to determine whether the track was a playlist or a single song from a playlist
     * @return a {@link MessageEmbed} for {@link GuildAudioManager#playListLoaded(String, AudioPlaylist, VoiceChannel, boolean)} to manage and send.
     */
    private MessageEmbed playlistLoadedMessage(String trackUrl, AudioPlaylist playlist, boolean singleSong) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        long rawTimeUntilPlay = 0;
        for (AudioTrack queue : scheduler.getTrackQueue().stream().toList()) {
            rawTimeUntilPlay = rawTimeUntilPlay + queue.getDuration();
        }
        rawTimeUntilPlay -= playlist.getTracks().get(0).getDuration();
        if (audioPlayer.getPlayingTrack() != null) {
            rawTimeUntilPlay += (audioPlayer.getPlayingTrack().getDuration() - audioPlayer.getPlayingTrack().getPosition());
        }
        if (rawTimeUntilPlay < 0) {
            rawTimeUntilPlay = 0;
        }
        String timeUntilPlay = DurationFormatUtils.formatDuration(rawTimeUntilPlay, "HH:mm:ss");
        if (singleSong) {
            embedBuilder.addField("Title: ", "`" + playlist.getTracks().get(0).getInfo().title + "`", false);
            embedBuilder.addField("Author: ", "`" + playlist.getTracks().get(0).getInfo().author + "`", false);
            embedBuilder.addField("Link: ", playlist.getTracks().get(0).getInfo().uri, false);
            embedBuilder.setAuthor("|   Adding song to queue...", null, requester.get(playlist.getTracks().get(0)).getAvatarUrl());
            embedBuilder.setFooter("From Playlist: ✅");
        } else {
            embedBuilder.setAuthor("|   Adding Playlist to queue...", null, requester.get(playlist.getTracks().get(0)).getAvatarUrl());
            embedBuilder.addField("Playlist Title: ", "`" + playlist.getName() + "`", false);
            embedBuilder.addField("Playlist Size: ", "`" + playlist.getTracks().size() + "`", false);
            embedBuilder.addField("Playlist Link: ", trackUrl, false);
        }
        embedBuilder.addField("Position in queue: ", "`" + scheduler.getTrackQueue().size() + "`", false);
        embedBuilder.addField("Estimated time until track plays: ", "`" + timeUntilPlay + "`", false);
        embedBuilder.setColor(Color.decode("#34d2eb"));
        return embedBuilder.build();
    }

    /**
     * @param trackUrl part of {@param channel} message.
     */
    public Message trackNotFound(String trackUrl) {
        MessageBuilder message = new MessageBuilder();
        message.append("Could not find: ").append(trackUrl);
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     * @param trackUrl  part of {@param channel} message.
     * @param exception is a non-blocking error.
     */
    public Message trackLoadFailed(String trackUrl, FriendlyException exception) {
        MessageBuilder message = new MessageBuilder();
        message.append("Could not play: ").append(trackUrl).append(" \n `Reason: ").append(exception.getLocalizedMessage()).append("`");
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     * @param track     loaded to player.
     * @param requester instance of {@link User} who requested the track.
     */
    public void setRequester(AudioTrack track, User requester) {
        this.requester.put(track, requester);
    }

    /**
     * @return A map of audioTracks and Users, should be used to obtain user from a given track.
     */
    @Nullable
    public Map<AudioTrack, User> getRequester() {
        return this.requester;
    }

    /**
     * @param guild             instance for the audio to be played in
     * @param guildAudioManager Instance of this class to load the track
     * @param track             track to be played
     * @param voiceChannel      to attach bot to.
     * @param playTop           boolean arg to add track to top of queue
     */
    private void play(Guild guild, GuildAudioManager guildAudioManager, AudioTrack track, VoiceChannel voiceChannel, boolean playTop) {

        attachToVoiceChannel(guild, voiceChannel);
        if (playTop) {
            ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
            trackList.add(0, track);
            scheduler.setQueue(new LinkedBlockingQueue<>(trackList));
            return;
        }
        guildAudioManager.scheduler.queue(track);
    }

    /**
     *  Skip's the current track by using {@link TrackScheduler#nextTrack()}
     */
    public Message skipTrack() {
        GuildAudioManager manager = getGuildAudioManager(currentGuild);
        AudioTrack track = manager.audioPlayer.getPlayingTrack();
        manager.scheduler.nextTrack();
        MessageBuilder message = new MessageBuilder();
        if (queueLoop || songLoop) {
            message.append("Track skipped! Loop was disabled!");
        } else {
            message.append("Skipped track: `").append(track.getInfo().author).append(" - ").append(track.getInfo().title).append("`!");
        }
        queueLoop = false;
        songLoop = false;
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     * @param guild   to find active voice channel in.
     * @param channel to attach to.
     */
    @SuppressWarnings("all")
    private void attachToVoiceChannel(Guild guild, VoiceChannel channel) {

        boolean inVoiceChannel = guild.getSelfMember().getVoiceState().inAudioChannel();


        if (!inVoiceChannel) {
            AudioManager manager = guild.getAudioManager();
            manager.openAudioConnection(channel);
            manager.setSendingHandler(sendHandler);
        }
    }

    /**
     * @param i volume # 1-100 for normal audio, 500 max (distortion)
     */
    public Message setVolume(int i) {
        MessageBuilder message = new MessageBuilder();
        if (i < 0 || i > 500) {
            message.append("Volume must be between 1-500!");
        } else {
            message.append("Volume is currently set at: ").append(String.valueOf(i));
        }
        audioPlayer.setVolume(i);
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    public static int queuePage;

    /**
     *                creates a Dynamic {@link MessageEmbed} with multiple {@link Button} to search pages for the current queue
     */
    public Message displayQueue() {
        if(audioPlayer.getPlayingTrack() == null) {
            MessageBuilder message = new MessageBuilder();
            message.append("Cannot display queue when no track is playing!");
            return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
        }

        queuePage = 1;

        StringBuilder queue = new StringBuilder();
        BlockingQueue<AudioTrack> tracks = scheduler.getTrackQueue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Current Song Queue");
        eb.setColor(Color.decode("#42f5c8"));

        ArrayList<AudioTrack> trackList = new ArrayList<>(tracks.stream().toList());
        for (int i = 0; i <= 10; i++) {
            try {
                AudioTrack track = trackList.get(i);
                queue.append("`").append(i + 1).append(". ").append(track.getInfo().author).append(" - ").append(track.getInfo().title).append("` \n");
            } catch (IndexOutOfBoundsException ex) {
                break;
            }
        }
        eb.addField("Current Song: " + audioPlayer.getPlayingTrack().getInfo().author + " - " + audioPlayer.getPlayingTrack().getInfo().title, queue.toString(), false);

        String pageNumber = "Page " + queuePage + "/" + (int) Math.ceil((float) scheduler.getTrackQueue().size() / 10);
        String trackInQueue = "Songs in Queue: " + trackList.size();
        long queueLength = 0;
        String queueLengthStr;
        for (AudioTrack audioTrack : trackList) {
            queueLength += audioTrack.getDuration();
        }
        if (queueLength < 3600000) {
            queueLengthStr = ("Queue Duration: [" + DurationFormatUtils.formatDuration(queueLength, "mm:ss") + "]");
        } else {
            queueLengthStr = ("Queue Duration: [" + DurationFormatUtils.formatDuration(queueLength, "HH:mm:ss") + "]");
        }
        eb.setFooter(pageNumber + " | " + trackInQueue + " | " + queueLengthStr);

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("firstPage:" + currentGuild.getId(), "⏪"));
        buttons.add(Button.primary("backPage:" + currentGuild.getId(), "◀️"));
        buttons.add(Button.danger("remove:" + currentGuild.getId(), "✖️"));
        buttons.add(Button.primary("nextPage:" + currentGuild.getId(), "▶️"));
        buttons.add(Button.primary("lastPage:" + currentGuild.getId(), "⏩"));

        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : (new MessageBuilder().setEmbeds(eb.build()).setActionRows(ActionRow.of(buttons)).build());

    }

    public static Map<Guild, Long> nowPlayingId = new HashMap<>();

    /**
     * @param guild   to send message to.
     * @param newSong called by {@link TrackScheduler#onTrackStart(AudioPlayer, AudioTrack)}
     */
    public void announceNextTrack(Guild guild, AudioTrack newSong) {
        if (djEnabled) {
            try {
                TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));
                assert channel != null;
                channel.sendMessageEmbeds(djEnabledEmbed(guild.getJDA())).queue();
                return;
            } catch (SQLException ex) {
                return;
            }
        }
        try {
            TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));

            EmbedBuilder eb = new EmbedBuilder();
            User trackSender = requester.get(newSong);
            if(trackSender == null) trackSender = guild.getJDA().getSelfUser();
            eb.setAuthor("|   Currently Playing...", null, trackSender.getAvatarUrl());
            eb.addField(newSong.getInfo().title, "By - " + newSong.getInfo().author, false);
            eb.setColor(Color.decode("#155b5e"));
            assert channel != null;
            List<Button> buttons = new ArrayList<>();
            buttons.add(Button.success("togglePlayer:" + channel.getGuild().getId(), "Pause/Resume"));
            buttons.add(Button.primary("skipTrack:" + channel.getGuild().getId(), "Skip"));
            buttons.add(Button.secondary("showQueue:" + channel.getGuild().getId(), "Show Queue"));
            buttons.add(Button.danger("remove:" + channel.getGuild().getId(), "✖️"));
            if (nowPlayingId.getOrDefault(guild, null) != null) {
                channel.deleteMessageById(nowPlayingId.get(guild)).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE).handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {}));
            }
            channel.sendMessageEmbeds(eb.build()).setActionRow(buttons).queue(message -> nowPlayingId.put(guild, message.getIdLong()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * toggles whether {@link AudioPlayer#isPaused()}
     */
    public void togglePlayer() {
        try {
            this.audioPlayer.setPaused(!audioPlayer.isPaused());
            if(audioPlayer.isPaused()) InactivityTimer.startInactivity(audioPlayer, currentGuild);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * copy of {@link GuildAudioManager#skipTrack()} without a confirmation message.
     */
    public void skipNoMessage() {
        try {
            this.scheduler.nextTrack();
            queueLoop = false;
            songLoop = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *                Clears current queue with {@link TrackScheduler#setQueue(BlockingQueue)} by creating a blank {@link LinkedBlockingQueue<AudioTrack>}
     */
    public Message clearQueue() {
        MessageBuilder message = new MessageBuilder();
        message.append("Clearing queue!");
        scheduler.setQueue(new LinkedBlockingQueue<>());
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();

    }

    /**
     *                if {@link AudioPlayer#isPaused()} does nothing, otherwise pauses player.
     */
    public Message pausePlayer() {
        MessageBuilder message = new MessageBuilder();
        if (!audioPlayer.isPaused()) {
            message.append("Paused ⏸️");
            audioPlayer.setPaused(true);
            if(audioPlayer.isPaused()) InactivityTimer.startInactivity(audioPlayer, currentGuild);
        }
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     *                if {@link AudioPlayer#isPaused()} unpauses the player.
     */
    public Message resumePlayer() {
        MessageBuilder message = new MessageBuilder();
        if (audioPlayer.isPaused()) {
            message.append("Resumed ▶️");
            audioPlayer.setPaused(false);
        }
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     *                obtains instance of queue with {@link TrackScheduler#getTrackQueue()} and randomizes it with collections.
     */
    public Message shufflePlayer() {
        MessageBuilder message = new MessageBuilder();
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        Collections.shuffle(trackList);
        BlockingQueue<AudioTrack> tracks = new LinkedBlockingQueue<>(trackList);
        scheduler.setQueue(tracks);
        message.append("Shuffling! \uD83C\uDFB2");
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     * creates {@link MessageEmbed} with song progress bar among other information about the playing track.
     */
    public Message sendTrackInfo() {
        if (audioPlayer.getPlayingTrack() == null) {
            MessageBuilder message = new MessageBuilder();
            message.append("Cannot get track info as no song is playing!");
            return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
        }
        AudioTrack track = audioPlayer.getPlayingTrack();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(track.getInfo().author + " - " + track.getInfo().title);
        String durationSlider = ("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        float div = ((float) track.getPosition() / (float) track.getDuration() * 20);
        int duration = Math.round(div);
        String emoji = ("\uD83D\uDD18");
        if(duration > 19) duration = 19;
            durationSlider = durationSlider.substring(0, duration) + emoji + durationSlider.substring(duration + 1);

        String time = "[" + DurationFormatUtils.formatDuration(track.getPosition(), "HH:mm:ss") + "/" + DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss") + "]";
        assert getRequester() != null;
        eb.addField("-Requested By: ", getRequester().get(track).getAsMention() + "\n" + durationSlider + "\n" + time, false);

        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : new MessageBuilder().setEmbeds(eb.build()).build();
    }

    /**
     * @param position of track in queue (adjusted for 0 index)
     */
    public Message removeTrack(int position) {
        BlockingQueue<AudioTrack> tracks = scheduler.getTrackQueue();
        AudioTrack removed = tracks.stream().toList().get(position - 1);
        MessageBuilder message = new MessageBuilder();
        if (removed != null) {
            message.append("Successfully Removed: `").append(removed.getInfo().author).append(" - ").append(removed.getInfo().title).append("` from the queue!");
            ArrayList<AudioTrack> trackList = new ArrayList<>(tracks.stream().toList());
            trackList.remove(position - 1);
            tracks = new LinkedBlockingQueue<>(trackList);
            scheduler.setQueue(tracks);
        } else {
            message.append("Could not locate track at position: ").append(String.valueOf(position)).append("!");
        }
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();

    }

    /**
     * @param time    using {@link DateTimeFormatter} to later be converted into MS
     */
    public Message seekTrack(String time) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            Function<String, String> adjust = input -> input.indexOf(":", 3) >= 0
                    ? input : "00:" + input;
            long millis = dtf.parse(adjust.apply(time)).get(ChronoField.MILLI_OF_DAY);
            audioPlayer.getPlayingTrack().setPosition(millis);
            MessageBuilder message = new MessageBuilder();
            message.append("Seeking to: ").append(time).append("!");
            return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
        } catch (Exception ex) {
            MessageBuilder message = new MessageBuilder();
            message.append("Invalid use of seek! Please use the format 'HH:mm:ss'");
            return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
        }
    }

    /**
     * @param track        private icecast server
     * @param voiceChannel to attach to
     */
    protected void djLoaded(AudioTrack track, VoiceChannel voiceChannel) {
        play(voiceChannel.getGuild(), getGuildAudioManager(voiceChannel.getGuild()), track, voiceChannel, false);
    }


    private BlockingQueue<AudioTrack> hijackQueue;

    /**
     * @param channel to block/send commands from
     * @param sender  instance of current DJ
     * @param guild   to manage
     */
    @SuppressWarnings("all")
    public Message enableDJ(User sender, Guild guild) {
        if (!djEnabled) {
            hijackQueue = new LinkedBlockingQueue<>(scheduler.getTrackQueue());
            clearQueue();
            skipTrack();
            djEnabled = true;
        } else {
            djEnabled = false;
            clearQueue();
            skipTrack();
            scheduler.setQueue(hijackQueue);
            MessageBuilder message = new MessageBuilder();
            message.append("So sad but, the party is over now! :(");
            return message.build();
        }
        VoiceChannel vc = (VoiceChannel) guild.getMember(sender).getVoiceState().getChannel();
        this.getAudioManager().loadItemOrdered(this, "http://10.0.0.109:8000/mixxx", new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                djLoaded(audioTrack, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
        return new MessageBuilder().setEmbeds(djEnabledEmbed(guild.getJDA())).build();


    }

    public boolean queueLoop = false;
    public boolean songLoop = false;

    /**
     *  manages {@link GuildAudioManager#queueLoop} & {@link GuildAudioManager#songLoop}
     */
    public Message loopQueue() {
        MessageBuilder message = new MessageBuilder();
        if (songLoop) {
            songLoop = false;
            queueLoop = false;
            message.append("Disabled queue loop! \uD83D\uDD01 \n");
        }

        if (queueLoop) {
            queueLoop = false;
            message.append("Disabled queue loop! \uD83D\uDD01");
            return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
        }
        queueLoop = true;
        message.append("Enabled queue loop! \uD83D\uDD01");
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     *  manages {@link GuildAudioManager#queueLoop} & {@link GuildAudioManager#songLoop}
     */
    public Message loopSong() {
        MessageBuilder message = new MessageBuilder();
        queueLoop = false;

        if (songLoop) {
            songLoop = false;
            message.append("Disabled song loop! \uD83D\uDD02");
            return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
        }
        songLoop = true;
        message.append("Enabled song loop! \uD83D\uDD02");
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     * @param pos1    is first track position to move.
     * @param pos2    is second track position to move
     *                swaps positions of {@param pos1} & {@param pos2}
     */
    public Message moveSong(int pos1, int pos2) {
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        AudioTrack song1 = trackList.get(pos1 - 1);
        Collections.swap(trackList, pos1 - 1, pos2 - 1);
        scheduler.setQueue(new LinkedBlockingQueue<>(trackList));
        MessageBuilder message = new MessageBuilder();
        message.append("Moved song `").append(song1.getInfo().author).append(" - ").append(song1.getInfo().title).append("` to position: `").append(String.valueOf(pos2)).append("`");
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

    /**
     * @param indexNumber to skip track queue to (adjusted for 0 index)
     */
    public Message skipTo(int indexNumber) {
        indexNumber -= 1;
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        int startSize = scheduler.getTrackQueue().size();
        if (indexNumber > 0 && scheduler.getTrackQueue().size() > indexNumber) {
            trackList.subList(0, indexNumber).clear();
        } else {
            trackList.clear();
        }
        scheduler.setQueue(new LinkedBlockingQueue<>(trackList));
        MessageBuilder message = new MessageBuilder();
        message.append("Successfully removed: ").append(String.valueOf((startSize - indexNumber) > 0 ? indexNumber : startSize)).append(" songs from the queue!");
        return djEnabled ? new MessageBuilder().setEmbeds(djEnabledEmbed(currentGuild.getJDA())).build() : message.build();
    }

}
