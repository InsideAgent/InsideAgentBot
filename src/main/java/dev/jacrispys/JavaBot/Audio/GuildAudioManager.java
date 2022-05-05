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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class GuildAudioManager extends ListenerAdapter {

    private final AudioPlayerManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final Map<AudioTrack, User> requester = new HashMap<>();

    private boolean djEnabled = false;

    private static final Map<Guild, GuildAudioManager> audioManagers = new HashMap<>();

    private static Guild currentGuild = null;

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
        instance.getJDA().addEventListener(this);
    }

    public AudioPlayerManager getAudioManager() {
        return this.audioManager;
    }

    public void trackLoaded(TextChannel channel, String trackUrl, AudioTrack track, VoiceChannel voiceChannel, boolean playTop) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessageEmbeds(songLoadedMessage(trackUrl, track)).queue();

        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel, playTop);
    }

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
            embedBuilder.setTitle("Adding song to queue...");
            embedBuilder.setFooter("From Playlist: ✅");
        } else {
            embedBuilder.setTitle("Adding playlist to queue...");
            embedBuilder.addField("Playlist Title: ", "`" + playlist.getName() + "`", false);
            embedBuilder.addField("Playlist Size: ", "`" + playlist.getTracks().size() + "`", false);
            embedBuilder.addField("Playlist Link: ", trackUrl, false);
        }
        embedBuilder.addField("Position in queue: ", "`" + scheduler.getTrackQueue().size() + "`", false);
        embedBuilder.addField("Estimated time until track plays: ", "`" + timeUntilPlay + "`", false);
        embedBuilder.setColor(Color.decode("#34d2eb"));
        return embedBuilder.build();
    }

    public void trackNotFound(TextChannel channel, String trackUrl) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Could not find: " + trackUrl).queue();
    }

    public void trackLoadFailed(TextChannel channel, String trackUrl, FriendlyException exception) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Could not play: " + trackUrl + " \nReason:" + exception.getMessage()).queue();
    }

    public void setRequester(AudioTrack track, User requester) {
        this.requester.put(track, requester);
    }

    @Nullable
    public Map<AudioTrack, User> getRequester() {
        return this.requester;
    }

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

    @SuppressWarnings("all")
    private void attachToVoiceChannel(Guild guild, VoiceChannel channel) {

        boolean inVoiceChannel = guild.getSelfMember().getVoiceState().inAudioChannel();


        if (!inVoiceChannel) {
            AudioManager manager = guild.getAudioManager();
            manager.openAudioConnection(channel);
            manager.setSendingHandler(sendHandler);
        }
    }

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

    private int queuePage;

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
        int queueLength = 0;
        String queueLengthStr;
        for(AudioTrack audioTrack : trackList) {
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

    private EmbedBuilder updateEmbed(MessageEmbed embed, int page) {
        queuePage = page;
        EmbedBuilder eb = new EmbedBuilder(embed);
        eb.clearFields();
        StringBuilder queue = new StringBuilder();
        ArrayList<AudioTrack> trackList = new ArrayList<>(scheduler.getTrackQueue().stream().toList());
        for (int i = 0; i <= 10; i++) {
            try {
                AudioTrack track = trackList.get((page - 1) * 10 + i);
                String time;
                if (track.getDuration() < 3600000) {
                    time = ("*[" + DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss") + "]*");
                } else {
                    time = ("*[" + DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss") + "]*");
                }
                queue.append("`").append((page - 1) * 10 + i + 1).append(". ").append(track.getInfo().author).append(" - ").append(track.getInfo().title).append(" ").append(time).append("` \n");
            } catch (IndexOutOfBoundsException ex) {
                break;
            }
        }
        String pageNumber = "Page " + page + "/" + (int) Math.ceil((float) scheduler.getTrackQueue().size() / 10);
        String trackInQueue = "Songs in Queue: " + trackList.size();
        int queueLength = 0;
        String queueLengthStr;
        for(AudioTrack audioTrack : trackList) {
            queueLength += audioTrack.getDuration();
        }
        if (queueLength < 3600000) {
            queueLengthStr = ("Queue Duration: [" + DurationFormatUtils.formatDuration(queueLength, "mm:ss") + "]");
        } else {
            queueLengthStr = ("Queue Duration: [" + DurationFormatUtils.formatDuration(queueLength, "HH:mm:ss") + "]");
        }
        eb.setFooter(pageNumber + " | " + trackInQueue + " | " + queueLengthStr);
        eb.addField("Current Song: " + audioPlayer.getPlayingTrack().getInfo().author + " - " + audioPlayer.getPlayingTrack().getInfo().title, queue.toString(), false);
        return eb;


    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            Guild fromButtonGuild = event.getJDA().getGuildById(event.getComponentId().split(":")[1]);
            String buttonName = event.getComponentId().split(":")[0];
            int pages = (int) Math.ceil((float) scheduler.getTrackQueue().size() / 10);
            if (fromButtonGuild != event.getGuild()) return;

            switch (buttonName) {
                case ("firstPage") -> {
                    if (queuePage != 1) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), 1).build()).queue();
                    } else {
                        event.reply("You are already on the first page!").setEphemeral(true).queue();
                    }
                }
                case ("backPage") -> {
                    if (queuePage <= 1) {
                        if (queuePage == 0) {
                            event.reply("What? Did you expect page 0 or... HEY WAIT A MINUTE \uD83D\uDE21").setEphemeral(true).queue();
                        } else {
                            event.reply("What? Did you expect page 0 or something?").setEphemeral(true).queue();
                        }
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), queuePage - 1).build()).queue();
                }
                case ("remove") -> event.getMessage().delete().queue();
                case ("nextPage") -> {
                    if (queuePage >= pages) {
                        event.reply("Cannot go further than the final page!").setEphemeral(true).queue();
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), queuePage + 1).build()).queue();
                }
                case ("lastPage") -> {
                    if (queuePage != pages) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), pages).build()).queue();
                    } else {
                        event.reply("You are already on the final page!").setEphemeral(true).queue();
                    }
                }
                default -> System.out.println("wtf");
            }
        } catch (IllegalStateException ex) {
        }
    }


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

            eb.setTitle("Currently Playing... \uD83C\uDFB5\uD83C\uDFB5\uD83C\uDFB5");
            eb.addField(newSong.getInfo().title, "By - " + newSong.getInfo().author, false);
            eb.setColor(Color.decode("#155b5e"));
            assert channel != null;
            channel.sendMessageEmbeds(eb.build()).queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void clearQueue(TextChannel channel) {
        if (djEnabled) {
            channel.sendMessage("Can't Access this command while the DJ is in charge! ヽ(⌐■_■)ノ♬").queue();
            return;
        }
        channel.sendMessage("Clearing queue!").queue();
        scheduler.setQueue(new LinkedBlockingQueue<>());
    }

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

    protected void djLoaded(TextChannel channel, AudioTrack track, VoiceChannel voiceChannel) {
        play(channel.getGuild(), getGuildAudioManager(channel.getGuild()), track, voiceChannel, false);
    }


    @SuppressWarnings("all")
    public void enableDJ(TextChannel channel, User sender, Guild guild) {
        if (!djEnabled) {
            clearQueue(channel);
            skipTrack(channel);
            djEnabled = true;
        } else {
            djEnabled = false;
            clearQueue(channel);
            skipTrack(channel);
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

}
