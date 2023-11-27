package dev.jacrispys.JavaBot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.jacrispys.JavaBot.utils.SpotifyManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static dev.jacrispys.JavaBot.audio.GuildAudioManager.nowPlayingId;
import static dev.jacrispys.JavaBot.audio.GuildAudioManager.queuePage;

/**
 * Event listener to handle buttons on paginated embeds.
 */
public class AudioPlayerButtons extends ListenerAdapter {

    private GuildAudioManager audioManager;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Listen's for a ButtonInteractionEvent and then checks and edit's an embed according to what each button is mapped to.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            Guild fromButtonGuild = event.getGuild();
            audioManager =  GuildAudioManager.getGuildAudioManager(fromButtonGuild);
            String buttonName = event.getComponentId().split(":")[0];
            int pages = (int) Math.ceil((float) audioManager.scheduler.getTrackQueue().size() / 10);
            if (fromButtonGuild != event.getGuild()) return;

            switch (buttonName) {
                case ("firstPage") -> {
                    logger.debug("{} -  First page button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    if (queuePage != 1) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), 1).build()).queue();
                    } else {
                        if (!event.isAcknowledged()) {
                            event.reply("You are already on the first page!").setEphemeral(true).queue();
                        }
                    }
                }
                case ("backPage") -> {
                    logger.debug("{} -  Back page button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    if (queuePage <= 1) {
                        if (queuePage == 0) {
                            if (!event.isAcknowledged()) {
                                event.reply("What? Did you expect page 0 or... HEY WAIT A MINUTE \uD83D\uDE21").setEphemeral(true).queue();
                            }
                        } else {
                            if (!event.isAcknowledged()) {
                                event.reply("What? Did you expect page 0 or something?").setEphemeral(true).queue();
                            }
                        }
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), (queuePage - 1)).build()).queue();
                }
                case ("remove") -> {
                    logger.debug("{} -  Remove button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    if(event.getMessage().isEphemeral()) {
                        event.editMessageEmbeds(new EmbedBuilder().setAuthor(".", null, Objects.requireNonNull(event.getGuild()).getSelfMember().getEffectiveAvatarUrl()).build()).queue();
                    } else event.getMessage().delete().queue();
                }
                case ("nextPage") -> {
                    logger.debug("{} -  Next page button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    if (queuePage >= pages) {
                        if (!event.isAcknowledged()) {
                            event.reply("Cannot go further than the final page!").setEphemeral(true).queue();
                        }
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), queuePage + 1).build()).queue();
                }
                case ("lastPage") -> {
                    logger.debug("{} -  Last page button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    if (queuePage != pages) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), pages).build()).queue();
                    } else {
                        if (!event.isAcknowledged()) {
                            event.reply("You are already on the final page!").setEphemeral(true).queue();
                        }
                    }
                }
                case ("togglePlayer") -> {
                    logger.debug("{} -  Pause button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    audioManager.togglePlayer();
                    event.deferEdit().queue();
                }
                case ("skipTrack") -> {
                    logger.debug("{} -  Skip track button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    nowPlayingId.put(fromButtonGuild, event.getMessage().getIdLong());
                    if(audioManager.audioPlayer.getPlayingTrack() == null) {
                        event.deferEdit().queue();
                    } else {
                        event.reply((MessageCreateData) audioManager.skipTrack(event.getMember())).queue();
                    }
                }
                case ("showQueue") -> {
                    logger.debug("{} -  Show Queue button pressed, ID (" + event.getComponentId() + ")", getClass().getSimpleName());
                    event.deferReply().setEphemeral(true).queue();
                    event.getInteraction().getHook().editOriginal((MessageEditData) audioManager.displayQueue()).queue();
                    if (!event.isAcknowledged()) {
                        event.editMessage((MessageEditData) event.getMessage()).queue();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * @param embed embed to update
     * @param page page of list to display
     * @return buildable embed to use as a reply in {@link AudioPlayerButtons#onButtonInteraction(ButtonInteractionEvent)}
     */
    private EmbedBuilder updateEmbed(MessageEmbed embed, int page) {
        queuePage = page;
        EmbedBuilder eb = new EmbedBuilder(embed);
        eb.clearFields();
        StringBuilder queue = new StringBuilder();
        StringBuilder queue2 = new StringBuilder();
        ArrayList<AudioTrack> trackList = new ArrayList<>(audioManager.scheduler.getTrackQueue().stream().toList());
        for (int i = 0; i < 10; i++) {
            try {
                AudioTrack track = queuePage == 1 ? trackList.get(i) : trackList.get((page - 1) * 10 + (i - 1));
                String time;
                if (track.getDuration() < 3600000) {
                    time = ("[" + DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss") + "]");
                } else {
                    time = ("[" + DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss") + "]");
                }
                String artistLink = "https://open.spotify.com/artist/" + SpotifyManager.getArtistId(track.getIdentifier());
                if (i < 5) {
                    queue.append((page - 1) * 10 + i + 1).append(". [").append(track.getInfo().author).append("](").append(artistLink).append(") - [").append(track.getInfo().title).append("](").append(track.getInfo().uri).append(") ").append(time).append(" \n");
                } else {
                    queue2.append((page - 1) * 10 + i + 1).append(". [").append(track.getInfo().author).append("](").append(artistLink).append(") - [").append(track.getInfo().title).append("](").append(track.getInfo().uri).append(") ").append(time).append(" \n");

                }
            } catch (IndexOutOfBoundsException | IOException ex) {
                break;
            }
        }
        String pageNumber = "Page " + page + "/" + (int) Math.ceil((float) audioManager.scheduler.getTrackQueue().size() / 10);
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
        eb.addField("Current Song: " + audioManager.audioPlayer.getPlayingTrack().getInfo().author + " - " + audioManager.audioPlayer.getPlayingTrack().getInfo().title, queue.toString(), false);
        eb.addField("-[Continued]-", queue2.toString(), false);
        return eb;


    }
}
