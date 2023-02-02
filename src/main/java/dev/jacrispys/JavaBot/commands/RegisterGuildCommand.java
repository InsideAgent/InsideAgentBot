package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import dev.jacrispys.JavaBot.webhooks.SpotifyStats;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Deprecated(forRemoval = true)
public class RegisterGuildCommand extends ListenerAdapter {
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        MySQLConnection connection = MySQLConnection.getInstance();
        try {
            if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains("!registerguild")) {
                if (connection.registerGuild(event.getGuild(), event.getGuildChannel().asTextChannel())) {
                    event.getMessage().reply("Guild successfully indexed!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
                } else {
                    event.getMessage().reply("Could not index guild as it was already found in our database!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
                }
                event.getMessage().delete().queueAfter(2, TimeUnit.SECONDS);
            } else if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains("!setticketchannel")) {
                connection.executeCommand("UPDATE guilds SET TicketChannel=" + event.getGuildChannel().getId() + " WHERE ID=" + event.getGuild().getId());
                event.getMessage().reply("Ticket channel successfully set!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!testwebhook")) {
                SpotifyStats spotifyStats = new SpotifyStats(event.getGuildChannel().asTextChannel());
                Webhook webhook = spotifyStats.setIcon(new URL("https://assets.entrepreneur.com/content/3x2/2000/20150616163611-spotify.jpeg")).setName("Spotify Analyzer").build();
                spotifyStats.sendMessage(webhook, "web hook testing wooo");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            event.getMessage().reply("An internal error occurred while trying to set the channel!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
        }
    }

}

