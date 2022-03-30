package com.jacrispys.JavaBot.Commands;

import com.jacrispys.JavaBot.Utils.ChannelStorageManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RegisterGuildCommand extends ListenerAdapter {
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        ChannelStorageManager storageManager = ChannelStorageManager.getInstance();
        if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains("!registerguild")) {
            if (storageManager.addNewGuild(event.getGuild())) {
                event.getMessage().reply("Guild successfully indexed!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            } else {
                event.getMessage().reply("Could not index guild as it was already found in our database!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            }
            event.getMessage().delete().queueAfter(2, TimeUnit.SECONDS);
        } else if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains("!setticketchannel")) {
            if (storageManager.setTicketChannel(event.getGuild(), event.getTextChannel())) {
                event.getMessage().reply("Ticket channel successfully set!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            } else {
                event.getMessage().reply("Could not set ticket channel as it was already found in our database!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            }
        }
    }

}

