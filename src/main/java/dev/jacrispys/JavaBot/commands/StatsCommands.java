package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatsCommands extends ListenerAdapter {


    private final Map<Long, Map.Entry<ResultSet, Long>> cachedResults = new HashMap<>();
    private final Map<Long, Map.Entry<ResultSet, Long>> guildCachedResults = new HashMap<>();


    public List<CommandData> initJdaCommands() {
        List<CommandData> commands = new ArrayList<>();
        Collections.addAll(commands,
                Commands.slash("stats", "Basic stats command.")
                        .addOption(OptionType.BOOLEAN, "visible", "Determines if the stats message will be visible to all.", false),
                Commands.slash("userstats", "Stats for a given targeted user")
                        .addOption(OptionType.USER, "user", "User to retrieve stats for.", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                        .addOption(OptionType.BOOLEAN, "usecache", "Bypasses caching system if false", true)
                        .addOption(OptionType.BOOLEAN, "visible", "Determines if the stats message will be visible to all.", false),

                Commands.slash("serverstats", "Stats for the whole server.").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                        .addOption(OptionType.BOOLEAN, "visible", "Determines if the stats message will be visible to all.", true)
                        .addOption(OptionType.BOOLEAN, "usecache", "Bypasses caching system if false", true));
        return commands;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("stats")) {
            boolean visible = event.getOption("visible") != null && event.getOption("visible").getAsBoolean();
            event.deferReply(!visible).queue();
            User user = event.getUser();
            MessageEmbed embed;
            try {
                embed = generateUserStats(user, event.getGuild(), false).build();
            } catch (Exception ex) {
                ex.printStackTrace();
                embed = new EmbedBuilder().setTitle("Error while retrieving stats. Please try again.").setColor(0x870e16).build();
            }
            event.getHook().editOriginalEmbeds(embed).queue();
        } else if (event.getName().equalsIgnoreCase("userstats")) {
            User user = event.getOption("user").getAsUser();
            boolean visible = event.getOption("visible") != null && event.getOption("visible").getAsBoolean();
            boolean useCache = event.getOption("usecache") != null && event.getOption("usecache").getAsBoolean();
            event.deferReply(!visible).queue();
            MessageEmbed embed;
            try {
                embed = generateUserStats(user, event.getGuild(), !useCache).build();
            } catch (Exception ex) {
                ex.printStackTrace();
                embed = new EmbedBuilder().setTitle("Error while retrieving stats. Please try again.").setColor(0x870e16).build();
            }
            event.getHook().editOriginalEmbeds(embed).queue();
        } else if (event.getName().equalsIgnoreCase("serverstats")) {
            boolean visible = event.getOption("visible") != null && event.getOption("visible").getAsBoolean();
            boolean useCache = event.getOption("usecache") != null && event.getOption("usecache").getAsBoolean();
            event.deferReply(!visible).queue();
            MessageEmbed embed;
            try {
                embed = generateServerStats(event.getGuild(), !useCache).build();
            } catch (Exception ex) {
                ex.printStackTrace();
                embed = new EmbedBuilder().setTitle("Error while retrieving stats. Please try again.").setColor(0x870e16).build();
            }
            event.getHook().editOriginalEmbeds(embed).queue();
        }
    }


    private EmbedBuilder generateUserStats(User user, Guild guild, boolean invalidateCache) throws Exception {
        ResultSet rs;
        boolean cached = false;
        if (cachedResults.containsKey(user.getIdLong()) && (System.currentTimeMillis() - cachedResults.get(user.getIdLong()).getValue()) / 1000L < 1800 && !invalidateCache) {
            rs = cachedResults.get(user.getIdLong()).getKey();
            cached = true;
        } else {
            rs = MySQLConnection.getInstance().queryCommand("SELECT * FROM audio_activity WHERE user_id=" + user.getIdLong() + " AND guild_id=" + guild.getIdLong() + ";");
            cachedResults.put(user.getIdLong(), new AbstractMap.SimpleEntry<>(rs, System.currentTimeMillis()));
        }
        rs.beforeFirst();
        rs.next();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Statistics for: " + user.getName(), null, user.getEffectiveAvatarUrl());
        StringBuilder sb = new StringBuilder();
        sb.append("Songs Queued: `").append(rs.getLong("song_queues")).append("` \n");
        sb.append("Playlists Queued: `").append(rs.getLong("playlist_queues")).append("` \n");
        long listen_time = rs.getLong("listen_time");
        String listen_string = millisToTime(listen_time);
        sb.append("Time Listened: `").append(listen_string).append("` \n");
        sb.append("Other users songs skipped: `").append(rs.getLong("skip_others")).append("` \n");
        eb.addField("User stats queried...", sb.toString(), false);
        if (cached) eb.setFooter("Stats cached and may be up to 30 min out of date.");
        eb.setColor(0x34eb8f);
        return eb;
    }

    private EmbedBuilder generateServerStats(Guild guild, boolean invalidateCache) throws Exception {
        ResultSet rs;
        boolean cached = false;
        if (guildCachedResults.containsKey(guild.getIdLong()) && (System.currentTimeMillis() - guildCachedResults.get(guild.getIdLong()).getValue()) / 1000L < 1800 && !invalidateCache) {
            rs = guildCachedResults.get(guild.getIdLong()).getKey();
            cached = true;
        } else {
            rs = MySQLConnection.getInstance().queryCommand("SELECT * FROM guild_general_stats WHERE ID=" + guild.getIdLong() + ";");
            guildCachedResults.put(guild.getIdLong(), new AbstractMap.SimpleEntry<>(rs, System.currentTimeMillis()));
        }
        rs.beforeFirst();
        rs.next();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Statistics for: " + guild.getName(), null, guild.getIconUrl());
        eb.setThumbnail(guild.getBannerUrl());
        StringBuilder sb = new StringBuilder();
        sb.append("Songs Queued: `").append(rs.getLong("play_counter")).append("` \n");
        sb.append("Player Paused: `").append(rs.getLong("pause_counter")).append("` \n");
        long listen_time = rs.getLong("playtime_millis");
        String listen_string = millisToTime(listen_time);
        sb.append("Total Time Listened: `").append(listen_string).append("` \n");
        sb.append("Hijack Events: `").append(rs.getLong("hijack_counter")).append("` \n");
        sb.append("Commands sent: `").append(rs.getLong("command_counter")).append("` \n");
        eb.addField("User stats queried...", sb.toString(), false);
        if (cached) eb.setFooter("Stats cached and may be up to 30 min out of date.");
        eb.setColor(0x34eb8f);
        return eb;
    }

    private String millisToTime(long listen_time) {

        String days = String.valueOf(TimeUnit.MILLISECONDS.toDays(listen_time)).replaceAll("^0+(?!$)", "");
        String hours = String.valueOf(TimeUnit.MILLISECONDS.toHours(listen_time) -
                TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(listen_time))).replaceAll("^0+(?!$)", "");
        String minutes = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(listen_time) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(listen_time))).replaceAll("^0+(?!$)", "");
        String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(listen_time) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(listen_time))).replaceAll("^0+(?!$)", "");
        return String.format("%s days, %s hours, %s minutes, %s seconds.",
                days,
                hours,
                minutes,
                seconds);
    }


}
