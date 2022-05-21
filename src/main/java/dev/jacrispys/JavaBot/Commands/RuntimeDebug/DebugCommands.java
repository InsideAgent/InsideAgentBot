package dev.jacrispys.JavaBot.Commands.RuntimeDebug;

import dev.jacrispys.JavaBot.Audio.GuildAudioManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DebugCommands extends ListenerAdapter {
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
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setAuthor("Active Audio Players - [DEBUG]", null, event.getAuthor().getEffectiveAvatarUrl());
                        StringBuilder builder = new StringBuilder();
                        activePlayers().forEach(guild -> builder.append("\n").append(guild.getName()));
                        eb.setFooter("Enabled Players: " + activePlayers().size() + " \uD83D\uDE40");
                        eb.addField("Guild with Active Players:", builder.toString(), false);
                        event.getMessage().replyEmbeds(eb.build()).queue();
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
