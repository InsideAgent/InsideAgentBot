package dev.jacrispys.JavaBot.Commands;

import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.audio.LoadAudioHandler;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

public class MusicCommands extends ListenerAdapter {


    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        LoadAudioHandler audioHandler = new LoadAudioHandler(audioManager);
        String message = event.getMessage().getContentRaw();
        if (((message.toLowerCase().contains("-play") && message.split("-play ").length > 1) || (message.toLowerCase().contains("-p") && message.split("-p ").length > 1))) {
            String trackUrl;
            if (message.toLowerCase().contains("-play")) {
                trackUrl = event.getMessage().getContentRaw().split("-play ")[1];
            } else {
                trackUrl = event.getMessage().getContentRaw().split("-p ")[1];
            }

            VoiceChannel channel;
            try {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                new URL(trackUrl);
                audioHandler.loadAndPlay(event.getTextChannel(), trackUrl, channel, event.getAuthor());
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getTextChannel().getIdLong());
                } catch(SQLException ex1) {
                    ex1.printStackTrace();
                }
            } catch(MalformedURLException ex) {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if(channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                String ytSearch = ("ytsearch:" + trackUrl);
                audioHandler.loadAndPlay(event.getTextChannel(), ytSearch, channel, event.getAuthor());
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getTextChannel().getIdLong());
                } catch(SQLException ex1) {
                    ex1.printStackTrace();
                }
            }
        }else if(message.equalsIgnoreCase("-skip") || message.equalsIgnoreCase("-s")) {
            audioHandler.skipTrack(audioManager, event.getTextChannel());
        }else if (event.getMessage().getContentRaw().contains("-volume")) {
            try {
            int i = Integer.parseInt(event.getMessage().getContentRaw().split("-volume ")[1]);

                audioManager.setVolume(i, event.getTextChannel());
            } catch(NumberFormatException ex) {
                event.getMessage().reply(event.getMessage().getContentRaw().split("-volume ")[1] + " is not a number 1 - 100!").queue();
            }
        }else if (message.equalsIgnoreCase("-clear") ) {
            audioManager.clearQueue(event.getTextChannel());
        }else if (message.equalsIgnoreCase("-pause") || message.equalsIgnoreCase("-stop")) {
            audioManager.pausePlayer(event.getTextChannel());
        }else if (message.equalsIgnoreCase("-resume") || message.equalsIgnoreCase("-play")) {
            audioManager.resumePlayer(event.getTextChannel());
        }else if (message.equalsIgnoreCase("-shuffle") ) {
            audioManager.shufflePlayer(event.getTextChannel());
        }else if (message.equalsIgnoreCase("-dc") || message.equalsIgnoreCase("-disconnect") || message.equalsIgnoreCase("-leave")) {
            event.getGuild().getAudioManager().closeAudioConnection();
            audioManager.clearQueue(event.getTextChannel());
        } else if(message.equalsIgnoreCase("-move") || message.equalsIgnoreCase("-follow")) {
            if(event.getGuild().getMember(event.getAuthor()).getVoiceState().inAudioChannel()) {
                event.getGuild().getAudioManager().openAudioConnection(event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel());
                event.getTextChannel().sendMessage("Following! ✈️").queue();
            } else {
                event.getTextChannel().sendMessage("You are not in a VoiceChannel that I can access!").queue();
            }
        } else if(message.equalsIgnoreCase("-song") || message.equalsIgnoreCase("-info")) {
            audioManager.sendTrackInfo(event.getTextChannel());
        }else if(message.equalsIgnoreCase("-q") || message.equalsIgnoreCase("-queue")) {
            audioManager.displayQueue(event.getTextChannel());
        } else if(message.toLowerCase().contains("-remove")) {
            try {
                int position = Integer.parseInt(event.getMessage().getContentRaw().split("-remove ")[1]);
                audioManager.removeTrack(position, event.getTextChannel());
            } catch(NumberFormatException ex) {
                event.getMessage().reply("Could not parse: " + event.getMessage().getContentRaw().split("-remove ")[1] + " as a number!").queue();

            }
        } else if(message.toLowerCase().contains("-seek")) {
            String time = event.getMessage().getContentRaw().split("-seek ")[1];
            audioManager.seekTrack(time, event.getTextChannel());
        } else if(message.equalsIgnoreCase("-hijack")) {
            if(event.getAuthor().getIdLong() != 731364923120025705L) {
                event.getMessage().reply("You sir! Are not a certified DJ! Begone! ヽ(⌐■_■)ノ♬").queue();
                return;
            }
            audioManager.enableDJ(event.getTextChannel(), event.getAuthor(), event.getGuild());
        } else if(message.equalsIgnoreCase("-fix")) {
            try {
                VoiceChannel vc = (VoiceChannel) event.getMember().getVoiceState().getChannel();
                vc.getManager().setRegion(Region.AUTOMATIC).queue();
                event.getMessage().addReaction("\uD83D\uDC4D").queue();
            } catch (NullPointerException ex) {
                event.getMessage().reply("Could not locate your voice channel!").queue();
            }
        } else if(message.toLowerCase().contains("-loop")) {
            if(message.split("-loop ").length == 1 || message.split("-loop ")[1].equalsIgnoreCase("queue")) {
                audioManager.loopQueue(event.getTextChannel());
            } else if(message.split("-loop ")[1].equalsIgnoreCase("song")) {
                audioManager.loopSong(event.getTextChannel());
            }
        }
    }
}
