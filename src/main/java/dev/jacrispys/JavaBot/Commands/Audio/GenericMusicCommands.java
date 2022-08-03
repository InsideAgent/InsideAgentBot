package dev.jacrispys.JavaBot.Commands.Audio;

import dev.jacrispys.JavaBot.Audio.GuildAudioManager;
import dev.jacrispys.JavaBot.Audio.LoadAudioHandler;
import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class GenericMusicCommands extends ListenerAdapter {


    @SuppressWarnings("all")
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        LoadAudioHandler audioHandler = new LoadAudioHandler(audioManager);
        String message = event.getMessage().getContentRaw().toLowerCase();
        if (message.startsWith("-")) {
            try {
                MySQLConnection connection = MySQLConnection.getInstance();
                ResultSet rs = connection.queryCommand("select * from inside_agent_bot.guilds where ID=" + event.getGuild().getId());
                rs.beforeFirst();
                if (!rs.next()) {
                    event.getGuildChannel().sendMessage("Cannot execute commands before guild is indexed! Please use `!registerguild` to index your guild!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    rs.close();
                    return;
                }
                rs.close();
            } catch (Exception ignored) {
                event.getGuildChannel().sendMessage("Cannot execute commands before guild is indexed! Please use `!registerguild` to index your guild!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
        }

        if (((message.contains("-play ") && message.split("-play ").length > 1) || (message.contains("-p ") && message.split("-p ").length > 1) || ((message.contains("-p ") || message.contains("-play ")) && event.getMessage().getAttachments().size() > 0))) {
            String trackUrl;
            if (event.getMessage().getAttachments().size() > 0) {
                VoiceChannel channel;
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if (channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                String track = event.getMessage().getAttachments().get(0).getUrl();
                event.getGuildChannel().sendMessage(audioHandler.loadAndPlay(track, channel, event.getAuthor(), false));
                return;

            } else if (message.contains("-play")) {
                trackUrl = event.getMessage().getContentRaw().split("-play ")[1];
            } else {
                trackUrl = event.getMessage().getContentRaw().split("-p ")[1];
            }


            VoiceChannel channel;
            try {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if (channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                new URL(trackUrl);
                event.getGuildChannel().sendMessage(audioHandler.loadAndPlay(trackUrl, channel, event.getAuthor(), false));
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getGuildChannel().getIdLong());
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                }
            } catch (MalformedURLException ex) {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if (channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                String ytSearch = ("ytsearch:" + trackUrl);
                event.getGuildChannel().sendMessage(audioHandler.loadAndPlay(ytSearch, channel, event.getAuthor(), false));
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getGuildChannel().getIdLong());
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                }
            }
        } else if (message.equalsIgnoreCase("-skip") || message.equalsIgnoreCase("-s")) {
            event.getGuildChannel().sendMessage(audioHandler.skipTrack(audioManager)).queue();
        } else if (event.getMessage().getContentRaw().contains("-volume")) {
            try {
                int i = Integer.parseInt(event.getMessage().getContentRaw().split("-volume ")[1]);
                event.getGuildChannel().sendMessage(audioManager.setVolume(i)).queue();
            } catch (NumberFormatException ex) {
                event.getMessage().reply(event.getMessage().getContentRaw().split("-volume ")[1] + " is not a number 1 - 100!").queue();
            }
        } else if (message.equalsIgnoreCase("-clear")) {
            event.getGuildChannel().sendMessage(audioManager.clearQueue()).queue();
        } else if (message.equalsIgnoreCase("-pause") || message.equalsIgnoreCase("-stop")) {
            event.getGuildChannel().sendMessage(audioManager.pausePlayer()).queue();
        } else if (message.equalsIgnoreCase("-resume") || message.equalsIgnoreCase("-play")) {
            event.getGuildChannel().sendMessage(audioManager.resumePlayer()).queue();
        } else if (message.equalsIgnoreCase("-shuffle")) {
            event.getGuildChannel().sendMessage(audioManager.shufflePlayer()).queue();
        } else if (message.equalsIgnoreCase("-dc") || message.equalsIgnoreCase("-disconnect") || message.equalsIgnoreCase("-leave")) {
            event.getGuildChannel().sendMessage(audioManager.disconnectBot()).queue();
        } else if (message.equalsIgnoreCase("-move") || message.equalsIgnoreCase("-follow")) {
            audioManager.followUser(event.getMember());
        } else if (message.equalsIgnoreCase("-song") || message.equalsIgnoreCase("-info")) {
            event.getGuildChannel().sendMessage(audioManager.sendTrackInfo()).queue();
        } else if (message.equalsIgnoreCase("-q") || message.equalsIgnoreCase("-queue")) {
            event.getGuildChannel().sendMessage(audioManager.displayQueue()).queue();
        } else if (message.contains("-remove")) {
            try {
                int position = Integer.parseInt(event.getMessage().getContentRaw().split("-remove ")[1]);
                event.getGuildChannel().sendMessage(audioManager.removeTrack(position)).queue();
            } catch (NumberFormatException ex) {
                event.getMessage().reply("Could not parse: " + event.getMessage().getContentRaw().split("-remove ")[1] + " as a number!").queue();

            }
        } else if (message.contains("-seek")) {
            String time = event.getMessage().getContentRaw().split("-seek ")[1];
            event.getGuildChannel().sendMessage(audioManager.seekTrack(time)).queue();
        } else if (message.equalsIgnoreCase("-hijack")) {
            if (event.getAuthor().getIdLong() != 731364923120025705L) {
                event.getMessage().reply("You sir! Are not a certified DJ! Begone! ヽ(⌐■_■)ノ♬").queue();
                return;
            }
            event.getGuildChannel().sendMessage(audioManager.enableDJ(event.getAuthor(), event.getGuild())).queue();
        } else if (message.equalsIgnoreCase("-fix")) {
            try {
                VoiceChannel vc = (VoiceChannel) event.getMember().getVoiceState().getChannel();
                vc.getManager().setRegion(Region.VIP_US_WEST).queue();
                vc.getManager().setRegion(Region.AUTOMATIC).queue();
                event.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDC4D")).queue();
            } catch (NullPointerException ex) {
                event.getMessage().reply("Could not locate your voice channel!").queue();
            }
        } else if (message.contains("-loop")) {
            if (message.split("-loop ").length == 1 || message.split("-loop ")[1].equalsIgnoreCase("queue")) {
                event.getGuildChannel().sendMessage(audioManager.loopQueue()).queue();
            } else if (message.split("-loop ")[1].equalsIgnoreCase("song")) {
                event.getGuildChannel().sendMessage(audioManager.loopSong()).queue();
            }
        } else if (message.contains("-move ")) {
            try {
                int pos1 = Integer.parseInt(message.split("-move ")[1].split(" ")[0]);
                int pos2 = Integer.parseInt(message.split("-move " + pos1 + " ")[1]);
                event.getGuildChannel().sendMessage(audioManager.moveSong(pos1, pos2)).queue();
            } catch (NumberFormatException ex) {
                event.getGuildChannel().sendMessage("Cannot parse integer positions! Please use the format: `-move [pos1] [pos2]` where pos1,pos2 are numbers!").queue();
            }
        } else if (((message.contains("-playtop") && message.split("-playtop ").length > 1) || (message.contains("-ptop") && message.split("-ptop ").length > 1))) {
            String trackUrl;
            if (message.contains("-playtop")) {
                trackUrl = event.getMessage().getContentRaw().split("-playtop ")[1];
            } else {
                trackUrl = event.getMessage().getContentRaw().split("-ptop ")[1];
            }

            VoiceChannel channel;
            try {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if (channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                new URL(trackUrl);
                event.getGuildChannel().sendMessage(audioHandler.loadAndPlay(trackUrl, channel, event.getAuthor(), true));
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getGuildChannel().getIdLong());
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                }
            } catch (MalformedURLException ex) {
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if (channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                String ytSearch = ("ytsearch:" + trackUrl);
                event.getGuildChannel().sendMessage(audioHandler.loadAndPlay(ytSearch, channel, event.getAuthor(), true));
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getGuildChannel().getIdLong());
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                }
            }
        } else if ((message.contains("-skipto") && message.split("-skipto ").length > 1) || (message.contains("-st") && message.split("-st ").length > 1)) {
            try {
                int indexNumber = 0;
                if (message.contains("-skipto")) {
                    indexNumber = Integer.parseInt(message.split("-skipto ")[1]);
                } else if (message.contains("-st")) {
                    indexNumber = Integer.parseInt(message.split("-st ")[1]);
                }
                event.getGuildChannel().sendMessage(audioManager.skipTo(indexNumber)).queue();
            } catch (NumberFormatException ignored) {
                event.getGuildChannel().sendMessage("Error: Cannot skip to a non-number value!").queue();
            }
        }
    }
}
