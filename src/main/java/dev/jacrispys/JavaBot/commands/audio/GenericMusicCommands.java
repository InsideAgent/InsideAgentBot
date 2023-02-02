package dev.jacrispys.JavaBot.commands.audio;

import dev.jacrispys.JavaBot.api.libs.utils.mysql.MySqlStats;
import dev.jacrispys.JavaBot.api.libs.utils.mysql.StatType;
import dev.jacrispys.JavaBot.audio.GenerateGenrePlaylist;
import dev.jacrispys.JavaBot.audio.GuildAudioManager;
import dev.jacrispys.JavaBot.audio.LoadAudioHandler;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Legacy music commands using '-' prefix rather than slash commands
 */
public class GenericMusicCommands extends ListenerAdapter {

    protected void updateMusicChannel(Guild guild, TextChannel channel) {
        try {
            MySQLConnection.getInstance().setMusicChannel(Objects.requireNonNull(guild), channel.getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * Handles all commands prefixed with '-'
     */
    @SuppressWarnings("all")
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        GuildAudioManager audioManager = GuildAudioManager.getGuildAudioManager(event.getGuild());
        LoadAudioHandler audioHandler = new LoadAudioHandler(audioManager);
        String message = event.getMessage().getContentRaw().toLowerCase();
        if (message.startsWith("-")) {
            try {
                MySqlStats stats = MySqlStats.getInstance();
                stats.incrementGuildStat(event.getGuild().getIdLong(), StatType.COMMAND_COUNTER);
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
            updateMusicChannel(event.getGuild(), event.getGuildChannel().asTextChannel());
            if (event.getMessage().getAttachments().size() > 0) {
                VoiceChannel channel;
                channel = (VoiceChannel) event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
                if (channel == null) {
                    event.getMessage().reply("Could not load song, as you are not in a voice channel!").queue();
                    return;
                }
                String track = event.getMessage().getAttachments().get(0).getUrl();
                event.getGuildChannel().sendMessage((MessageCreateData) audioHandler.loadAndPlay(track, channel, event.getMember(), false, false)).queue();
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
                event.getGuildChannel().sendMessage((MessageCreateData) audioHandler.loadAndPlay(trackUrl, channel, event.getMember(), false, false)).queue();
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
                event.getGuildChannel().sendMessage((MessageCreateData) audioHandler.loadAndPlay(ytSearch, channel, event.getMember(), false, false)).queue();
                try {
                    MySQLConnection.getInstance().setMusicChannel(event.getGuild(), event.getGuildChannel().getIdLong());
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                }
            }
        } else if (message.equalsIgnoreCase("-skip") || message.equalsIgnoreCase("-s")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioHandler.skipTrack(audioManager, event.getMember())).queue();
        } else if (event.getMessage().getContentRaw().contains("-volume")) {
            try {
                int i = Integer.parseInt(event.getMessage().getContentRaw().split("-volume ")[1]);
                event.getGuildChannel().sendMessage((MessageCreateData) audioManager.setVolume(i)).queue();
            } catch (NumberFormatException ex) {
                event.getMessage().reply(event.getMessage().getContentRaw().split("-volume ")[1] + " is not a number 1 - 100!").queue();
            }
        } else if (message.equalsIgnoreCase("-clear")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.clearQueue()).queue();
        } else if (message.equalsIgnoreCase("-pause") || message.equalsIgnoreCase("-stop")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.pausePlayer()).queue();
        } else if (message.equalsIgnoreCase("-resume") || message.equalsIgnoreCase("-play")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.resumePlayer()).queue();
        } else if (message.equalsIgnoreCase("-shuffle")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.shufflePlayer()).queue();
        } else if (message.equalsIgnoreCase("-dc") || message.equalsIgnoreCase("-disconnect") || message.equalsIgnoreCase("-leave")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.disconnectBot(event.getMember())).queue();
        } else if (message.equalsIgnoreCase("-move") || message.equalsIgnoreCase("-follow")) {
            audioManager.followUser(event.getMember());
        } else if (message.equalsIgnoreCase("-song") || message.equalsIgnoreCase("-info")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.sendTrackInfo()).queue();
        } else if (message.equalsIgnoreCase("-q") || message.equalsIgnoreCase("-queue")) {
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.displayQueue()).queue();
        } else if (message.contains("-remove")) {
            try {
                int position = Integer.parseInt(event.getMessage().getContentRaw().split("-remove ")[1]);
                event.getGuildChannel().sendMessage((MessageCreateData) audioManager.removeTrack(position)).queue();
            } catch (NumberFormatException ex) {
                event.getMessage().reply("Could not parse: " + event.getMessage().getContentRaw().split("-remove ")[1] + " as a number!").queue();
            } catch (ArrayIndexOutOfBoundsException ex) {
                event.getMessage().reply("Could not remove position: " + event.getMessage().getContentRaw().split("-remove ")[1] + " as it is not in bounds of the current queue.").queue();
            }
        } else if (message.contains("-seek")) {
            String time = event.getMessage().getContentRaw().split("-seek ")[1];
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.seekTrack(time)).queue();
        } else if (message.equalsIgnoreCase("-hijack")) {
            if (event.getAuthor().getIdLong() != 731364923120025705L) {
                event.getMessage().reply("You sir! Are not a certified DJ! Begone! ヽ(⌐■_■)ノ♬").queue();
                return;
            }
            event.getGuildChannel().sendMessage((MessageCreateData) audioManager.enableDJ(event.getAuthor(), event.getGuild())).queue();
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
                event.getGuildChannel().sendMessage((MessageCreateData) audioManager.loopQueue()).queue();
            } else if (message.split("-loop ")[1].equalsIgnoreCase("song")) {
                event.getGuildChannel().sendMessage((MessageCreateData) audioManager.loopSong()).queue();
            }
        } else if (message.contains("-move ")) {
            try {
                int pos1 = Integer.parseInt(message.split("-move ")[1].split(" ")[0]);
                int pos2 = Integer.parseInt(message.split("-move " + pos1 + " ")[1]);
                event.getGuildChannel().sendMessage((MessageCreateData) audioManager.moveSong(pos1, pos2)).queue();
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                event.getGuildChannel().sendMessage("Cannot parse integer positions! Please use the format: `-move [pos1] [pos2]` where pos1,pos2 are numbers!").queue();
            }
        } else if (((message.contains("-playtop") && message.split("-playtop ").length > 1) || (message.contains("-ptop") && message.split("-ptop ").length > 1))) {
            updateMusicChannel(event.getGuild(), event.getGuildChannel().asTextChannel());
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
                event.getGuildChannel().sendMessage((MessageCreateData) audioHandler.loadAndPlay(trackUrl, channel, event.getMember(), true, false)).queue();
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
                event.getGuildChannel().sendMessage((MessageCreateData) audioHandler.loadAndPlay(ytSearch, channel, event.getMember(), true, false)).queue();
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
                event.getGuildChannel().sendMessage((MessageCreateData) audioManager.skipTo(indexNumber)).queue();
            } catch (NumberFormatException ignored) {
                event.getGuildChannel().sendMessage("Error: Cannot skip to a non-number value!").queue();
            }
        } else if ((message.contains("-radio")) && message.split("-radio ").length == 2) {
            if(GenerateGenrePlaylist.reactMessage.containsKey(event.getAuthor())) {
                event.getMessage().reply("Cannot use this command until current request has been fulfilled!").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            int limit;
            try {
                limit = Integer.parseInt(message.split("-radio ")[1]);
            } catch (NumberFormatException ex) {
                event.getMessage().reply("Must have a number as first argument for: `-radio [limit]`!").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            if (limit > 100) {
                event.getMessage().reply("Cannot add more than 100 songs to radio!").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }
            GenerateGenrePlaylist.limit.put(event.getAuthor(), limit);
            event.getMessage().reply((MessageCreateData) audioManager.genreList(event.getAuthor().getIdLong())).queue(m -> {
                String[] ones = {"zero", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"};
                for (int i = 0; i < 10; i++) {
                    m.addReaction(Emoji.fromUnicode(ones[i + 1])).queue();
                }
                GenerateGenrePlaylist.reactMessage.put(event.getAuthor(), m.getIdLong());
            });
        }
    }
}
