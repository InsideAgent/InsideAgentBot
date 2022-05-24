package dev.jacrispys.JavaBot.Commands.RuntimeDebug;

import dev.jacrispys.JavaBot.Audio.GuildAudioManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DebugCommands extends ListenerAdapter {
    @SuppressWarnings("all")
    private final long DEBUG_SERVER = 786741501014441995L;


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getGuild() == event.getJDA().getGuildById(DEBUG_SERVER)) {
            if (event.getAuthor().isBot()) return;
            String message;
            message = (event.getMessage().getContentRaw().toLowerCase().contains("-debug ") ? event.getMessage().getContentRaw().replace("-debug ", "").toLowerCase() : null);
            if (message != null) {
                assert event.getMember() != null;
                if(!event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                    boolean isDebug = false;
                    for(Role role : event.getMember().getRoles()) {
                        if(role.getName().equalsIgnoreCase("Debug")) {
                            isDebug = true;
                        }
                    }
                    if(!isDebug) return;
                }
                switch (message) {
                    case ("getactive"):
                    case("active"):
                    case("players"):
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setAuthor("Latency Info - [DEBUG]", null, event.getAuthor().getEffectiveAvatarUrl());
                        StringBuilder builder = new StringBuilder();
                        activePlayers().forEach(guild -> builder.append("\n").append(guild.getName()));
                        eb.setFooter("Enabled Players: " + activePlayers().size() + " \uD83D\uDE40");
                        eb.addField("Guild with Active Players:", builder.toString(), false);
                        event.getMessage().replyEmbeds(eb.build()).queue();
                        break;
                    case("latency"):
                    case("ping"):
                        EmbedBuilder latencyEb = new EmbedBuilder();
                        latencyEb.setAuthor("Active Audio Players - [DEBUG]", null, event.getAuthor().getEffectiveAvatarUrl());
                        long latency = Instant.now().toEpochMilli() - (event.getMessage().getTimeCreated().toInstant().toEpochMilli());
                        latencyEb.addField("Latency: ", "Gateway Latency: `" + event.getJDA().getGatewayPing() + "`Ms \n" + "Server latency: `" + latency + "`Ms", false);
                        latencyEb.setFooter("Negative ping what a joke :(");
                        event.getMessage().replyEmbeds(latencyEb.build()).queue();
                    case(""):
                    default:
                        break;
                }
            }
        }
    }

    private List<Guild> activePlayers() {
        List<Guild> activePlayers = new ArrayList<>();
        GuildAudioManager.getAudioManagers().forEach((guild, guildAudioManager) -> {
            if (GuildAudioManager.getGuildAudioManager(guild).audioPlayer.getPlayingTrack() != null) {
                activePlayers.add(guild);
            }
        });
        return activePlayers;
    }



}
