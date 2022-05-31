package dev.jacrispys.JavaBot.Audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import static dev.jacrispys.JavaBot.Audio.GuildAudioManager.nowPlayingId;
import static dev.jacrispys.JavaBot.Audio.GuildAudioManager.queuePage;

public class AudioPlayerButtons extends ListenerAdapter {

    private GuildAudioManager audioManager;

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
                    if (queuePage != 1) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), 1).build()).queue();
                    } else {
                        if (!event.isAcknowledged()) {
                            event.reply("You are already on the first page!").setEphemeral(true).queue();
                        }
                    }
                }
                case ("backPage") -> {
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
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), queuePage - 1).build()).queue();
                }
                case ("remove") -> {
                    if(event.getMessage().isEphemeral()) {
                        event.editMessageEmbeds(new EmbedBuilder().setAuthor(".", null, Objects.requireNonNull(event.getGuild()).getSelfMember().getEffectiveAvatarUrl()).build()).queue();
                    } else event.getMessage().delete().queue();
                }
                case ("nextPage") -> {
                    if (queuePage >= pages) {
                        if (!event.isAcknowledged()) {
                            event.reply("Cannot go further than the final page!").setEphemeral(true).queue();
                        }
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), queuePage + 1).build()).queue();
                }
                case ("lastPage") -> {
                    if (queuePage != pages) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), pages).build()).queue();
                    } else {
                        if (!event.isAcknowledged()) {
                            event.reply("You are already on the final page!").setEphemeral(true).queue();
                        }
                    }
                }
                case ("togglePlayer") -> {
                    audioManager.togglePlayer();
                    if (!event.isAcknowledged()) {
                        event.editMessage(event.getMessage()).queue();
                    }
                }
                case ("skipTrack") -> {
                    event.deferReply().queue();
                    nowPlayingId.put(fromButtonGuild, event.getMessage().getIdLong());
                    if(audioManager.audioPlayer.getPlayingTrack() == null) {
                        event.getHook().editOriginal(event.getMessage()).queue();
                    } else {
                        event.getHook().editOriginal(audioManager.skipTrack()).queue();
                    }
                }
                case ("showQueue") -> {
                    event.reply(audioManager.displayQueue()).setEphemeral(true).queue();
                    if (!event.isAcknowledged()) {
                        event.editMessage(event.getMessage()).queue();
                    }
                }
                default -> System.out.println("wat");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private EmbedBuilder updateEmbed(MessageEmbed embed, int page) {
        queuePage = page;
        EmbedBuilder eb = new EmbedBuilder(embed);
        eb.clearFields();
        StringBuilder queue = new StringBuilder();
        ArrayList<AudioTrack> trackList = new ArrayList<>(audioManager.scheduler.getTrackQueue().stream().toList());
        for (int i = 0; i <= 10; i++) {
            try {
                AudioTrack track = trackList.get((page - 1) * 10 + i);
                String time;
                if (track.getDuration() < 3600000) {
                    time = ("[" + DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss") + "]");
                } else {
                    time = ("[" + DurationFormatUtils.formatDuration(track.getDuration(), "HH:mm:ss") + "]");
                }
                queue.append("`").append((page - 1) * 10 + i + 1).append(". ").append(track.getInfo().author).append(" - ").append(track.getInfo().title).append(" ").append(time).append("` \n");
            } catch (IndexOutOfBoundsException ex) {
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
        return eb;


    }
}
