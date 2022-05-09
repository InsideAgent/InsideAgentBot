package dev.jacrispys.JavaBot.Audio;

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class GuildAudioManager {

    private final AudioPlayerManager audioManager;
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final Map<AudioTrack, User> requester = new HashMap<>();

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
            audioManagers.put(guild, audioManager);
            if (currentGuild == null) currentGuild = guild;
            return audioManager;
        }
        if (currentGuild == null) currentGuild = guild;
        return audioManagers.get(guild);
    }


    protected GuildAudioManager(Guild instance) {
        this.audioManager = new DefaultAudioPlayerManager();
        this.audioPlayer = audioManager.createPlayer();
        AudioSourceManagers.registerLocalSource(audioManager);

        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(System.getenv("SpotifyClientId"));
        spotifyConfig.setClientSecret(System.getenv("SpotifySecret"));
        spotifyConfig.setCountryCode("US");
        audioManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, audioManager));

        currentGuild = instance;

        AudioSourceManagers.registerRemoteSources(audioManager);
        this.scheduler = new TrackScheduler(this.audioPlayer, currentGuild);
        audioPlayer.addListener(this.scheduler);
        sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    /**
     * @return instance of AudioManager
     */
    public AudioPlayerManager getAudioManager() {
        return this.audioManager;
    }

    /**
     * @param channel      to send messages in
     * @param trackUrl     of the loaded track
     * @param track        instance of loaded track
     * @param voiceChannel to attach bot to
     * @param playTop      is {@link Boolean} for whether the loaded track should be placed at the top of the queue
     */
    public void trackLoaded(TextChannel channel, String trackUrl, AudioTrack track, VoiceChannel voiceChannel, boolean playTop) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessageEmbeds(songLoadedMessage(trackUrl, track)).queue();

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel, playTop);
    }

    /**
     * @param channel      to send messages in
     * @param trackUrl     of the loaded track
     * @param playlist     instance of loaded playlist
     * @param voiceChannel to attach bot to
     * @param playTop      is {@link Boolean} for whether the loaded track should be placed at the top of the queue
     */
    public void playListLoaded(TextChannel channel, String trackUrl, AudioPlaylist playlist, VoiceChannel voiceChannel, boolean playTop) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }


        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), firstTrack, voiceChannel, playTop);

        if (!playlist.isSearchResult()) {
            channel.sendMessageEmbeds(playlistLoadedMessage(trackUrl, playlist, false)).queue();
            for (int i = 1; i < playlist.getTracks().size(); i++) {
                play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), playlist.getTracks().get(i), voiceChannel, false);
            }
        } else {
            channel.sendMessageEmbeds(playlistLoadedMessage(trackUrl, playlist, true)).queue();
        }
    }

    /**
     * @param trackUrl of loaded song
     * @param track    instance of loaded track
     * @return a {@link MessageEmbed} to be sent and managed by {@link GuildAudioManager#trackLoaded(TextChannel, String, AudioTrack, VoiceChannel, boolean)} }
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
     * @return a {@link MessageEmbed} for {@link GuildAudioManager#playListLoaded(TextChannel, String, AudioPlaylist, VoiceChannel, boolean)} to manage and send.
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
     * @param channel  to send error message to.
     * @param trackUrl part of {@param channel} message.
     */
    public void trackNotFound(TextChannel channel, String trackUrl) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Could not find: " + trackUrl).queue();
    }

    /**
     * @param channel   to send error message to.
     * @param trackUrl  part of {@param channel} message.
     * @param exception is a non-blocking error.
     */
    public void trackLoadFailed(TextChannel channel, String trackUrl, FriendlyException exception) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Could not play: " + trackUrl + " \nReason:" + exception.getMessage()).queue();
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
     * @param channel to send confirmation message to.
     *                Skip's the current track by using {@link TrackScheduler#nextTrack()}
     */
    public void skipTrack(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        GuildAudioManager manager = getGuildAudioManager(channel.getGuild());
        manager.scheduler.nextTrack();
        if (queueLoop || songLoop) {
            channel.sendMessage("Track skipped! Loop was disabled!").queue();
        }
        queueLoop = false;
        songLoop = false;
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
     * @param i       volume # 1-100 for normal audio, 500 max (distortion)
     * @param channel to send confirmation message.
     */
    public void setVolume(int i, TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (i < 0 || i > 500) {
            channel.sendMessage("Volume must be between 1-500!").queue();
            return;
        }
        channel.sendMessage("Volume is currently set at: " + i).queue();
        audioPlayer.setVolume(i);
    }

    public static int queuePage;

    /**
     * @param channel to send confirmation message to.
     *                creates a Dynamic {@link MessageEmbed} with multiple {@link Button} to search pages for the current queue
     */
    public void displayQueue(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
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
        buttons.add(Button.primary("firstPage:" + channel.getGuild().getId(), "⏪"));
        buttons.add(Button.primary("backPage:" + channel.getGuild().getId(), "◀️"));
        buttons.add(Button.danger("remove:" + channel.getGuild().getId(), "✖️"));
        buttons.add(Button.primary("nextPage:" + channel.getGuild().getId(), "▶️"));
        buttons.add(Button.primary("lastPage:" + channel.getGuild().getId(), "⏩"));

        channel.sendMessageEmbeds(eb.build()).setActionRow(buttons).queue();

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
                channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
                return;
            } catch (SQLException ex) {
                return;
            }
        }
        try {
            TextChannel channel = guild.getTextChannelById(MySQLConnection.getInstance().getMusicChannel(guild));

            EmbedBuilder eb = new EmbedBuilder();
            User trackSender = requester.get(newSong);
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
                channel.deleteMessageById(nowPlayingId.get(guild)).queue();
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * copy of {@link GuildAudioManager#skipTrack(TextChannel)} without a confirmation message.
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
     * @param channel to send confirmation message to.
     *                Clears current queue with {@link TrackScheduler#setQueue(BlockingQueue)} by creating a blank {@link LinkedBlockingQueue<AudioTrack>}
     */
    public void clearQueue(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Clearing queue!").queue();
        scheduler.setQueue(new LinkedBlockingQueue<>());
    }

    /**
     * @param channel to send confirmation message to.
     *                if {@link AudioPlayer#isPaused()} does nothing, otherwise pauses player.
     */
    public void pausePlayer(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (!audioPlayer.isPaused()) {
            channel.sendMessage("Paused ⏸️").queue();
            audioPlayer.setPaused(true);
        }
    }

    /**
     * @param channel to send confirmation message to.
     *                if {@link AudioPlayer#isPaused()} unpauses the player.
     */
    public void resumePlayer(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (audioPlayer.isPaused()) {
            channel.sendMessage("Resumed ▶️").queue();
            audioPlayer.setPaused(false);
        }
    }

    /**
     *
     * @param channel to send confirmation to.
     *                obtains instance of queue with {@link TrackScheduler#getTrackQueue()} and randomizes it with collections.
     */
    public void shufflePlayer(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        Collections.shuffle(trackList);
        BlockingQueue<AudioTrack> tracks = new LinkedBlockingQueue<>(trackList);
        scheduler.setQueue(tracks);
        channel.sendMessage("Shuffling! \uD83C\uDFB2").queue();
    }

    /**
     *
     * @param channel to send info to.
     *                creates {@link MessageEmbed} with song progress bar among other information about the playing track.
     */
    public void sendTrackInfo(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessage("Cannot get track info as no song is playing!").queue();
            return;
        }
        AudioTrack track = audioPlayer.getPlayingTrack();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(track.getInfo().author + " - " + track.getInfo().title);
        String durationSlider = ("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        float div = ((float) track.getPosition() / (float) track.getDuration() * 20);
        int duration = Math.round(div);
        String emoji = ("\uD83D\uDD18");
        if (duration > 20) duration = 20;
        durationSlider = durationSlider.substring(0, duration) + emoji + durationSlider.substring(duration + 1);
        String time = "[" + DurationFormatUtils.formatDuration(track.getPosition(), "HH:mm:ss") + "/" + DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss") + "]";
        assert getRequester() != null;
        eb.addField("-Requested By: ", getRequester().get(track).getAsMention() + "\n" + durationSlider + "\n" + time, false);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     *
     * @param position of track in queue (adjusted for 0 index)
     * @param channel to send confirmation to.
     */
    public void removeTrack(int position, TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        BlockingQueue<AudioTrack> tracks = scheduler.getTrackQueue();
        AudioTrack removed = tracks.stream().toList().get(position - 1);
        if (removed != null) {
            channel.sendMessage("Successfully Removed: `" + removed.getInfo().author + " - " + removed.getInfo().title + "` from the queue!").queue();
            ArrayList<AudioTrack> trackList = new ArrayList<>(tracks.stream().toList());
            trackList.remove(position - 1);
            tracks = new LinkedBlockingQueue<>(trackList);
            scheduler.setQueue(tracks);
        } else {
            channel.sendMessage("Could not locate track at position: " + position + "!").queue();
        }

    }

    /**
     *
     * @param time using {@link DateTimeFormatter} to later be converted into MS
     * @param channel to send confirmation to.
     */
    public void seekTrack(String time, TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            Function<String, String> adjust = input -> input.indexOf(":", 3) >= 0
                    ? input : "00:" + input;
            long millis = dtf.parse(adjust.apply(time)).get(ChronoField.MILLI_OF_DAY);
            audioPlayer.getPlayingTrack().setPosition(millis);
            channel.sendMessage("Seeking to: " + time + "!").queue();
        } catch (Exception ex) {
            channel.sendMessage("Invalid use of seek! Please use the format 'HH:mm:ss'").queue();
        }
    }

    /**
     *
     * @param channel to block commands from
     * @param track private icecast server
     * @param voiceChannel to attach to
     */
    protected void djLoaded(TextChannel channel, AudioTrack track, VoiceChannel voiceChannel) {
        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel, false);
    }


    private BlockingQueue<AudioTrack> hijackQueue;

    /**
     *
     * @param channel to block/send commands from
     * @param sender instance of current DJ
     * @param guild to manage
     */
    @SuppressWarnings("all")
    public void enableDJ(TextChannel channel, User sender, Guild guild) {
        if (!djEnabled) {
            hijackQueue = new LinkedBlockingQueue<>(scheduler.getTrackQueue());
            clearQueue(channel);
            skipTrack(channel);
            djEnabled = true;
        } else {
            djEnabled = false;
            clearQueue(channel);
            skipTrack(channel);
            scheduler.setQueue(hijackQueue);
            channel.sendMessage("So sad but, the party is over now! :(").queue();
            return;
        }
        VoiceChannel vc = (VoiceChannel) guild.getMember(sender).getVoiceState().getChannel();
        this.getAudioManager().loadItemOrdered(this, "http://10.0.0.109:8000/mixxx", new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                djLoaded(channel, audioTrack, vc);
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
        channel.sendMessage("This bot has now been taken over by DJ " + sender.getAsMention() + "! ヽ(⌐■_■)ノ♬").queue();


    }

    public boolean queueLoop = false;
    public boolean songLoop = false;

    /**
     *
     * @param channel to send confirmation to.
     *                manages {@link GuildAudioManager#queueLoop} & {@link GuildAudioManager#songLoop}
     */
    public void loopQueue(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        if (songLoop) {
            songLoop = false;
            queueLoop = false;
            channel.sendMessage("Disabled queue loop! \uD83D\uDD01").queue();
        }

        if (queueLoop) {
            queueLoop = false;
            channel.sendMessage("Disabled queue loop! \uD83D\uDD01").queue();
            return;
        }
        queueLoop = true;
        channel.sendMessage("Enabled queue loop! \uD83D\uDD01").queue();
    }

    /**
     *
     * @param channel to send confirmation to.
     *                manages {@link GuildAudioManager#queueLoop} & {@link GuildAudioManager#songLoop}
     */
    public void loopSong(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        queueLoop = false;

        if (songLoop) {
            songLoop = false;
            channel.sendMessage("Disabled song loop! \uD83D\uDD02").queue();
            return;
        }
        songLoop = true;
        channel.sendMessage("Enabled song loop! \uD83D\uDD02").queue();
    }

    /**
     *
     * @param channel to send confirmation to.
     * @param pos1 is first track position to move.
     * @param pos2 is second track position to move
     *             swaps positions of {@param pos1} & {@param pos2}
     */
    public void moveSong(TextChannel channel, int pos1, int pos2) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        AudioTrack song1 = trackList.get(pos1 - 1);
        Collections.swap(trackList, pos1 - 1, pos2 - 1);
        scheduler.setQueue(new LinkedBlockingQueue<>(trackList));
        channel.sendMessage("Moved song `" + song1.getInfo().author + " - " + song1.getInfo().title + "` to position: `" + pos2 + "`").queue();
    }

    /**
     *
     * @param channel to send confirmation to.
     * @param indexNumber to skip track queue to (adjusted for 0 index)
     */
    public void skipTo(TextChannel channel, int indexNumber) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        indexNumber -= 1;
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        int startSize = scheduler.getTrackQueue().size();
        if (indexNumber > 0 && scheduler.getTrackQueue().size() > indexNumber) {
            trackList.subList(0, indexNumber).clear();
        } else {
            trackList.clear();
        }
        scheduler.setQueue(new LinkedBlockingQueue<>(trackList));
        channel.sendMessage("Successfully removed: " + ((startSize - indexNumber) > 0 ? indexNumber : startSize) + " songs from the queue!").queue();
    }

}
