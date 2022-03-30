package com.jacrispys.JavaBot.Commands.PrivateMessageCommands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DefaultPrivateMessageResponse extends ListenerAdapter {
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.isFromType(ChannelType.PRIVATE)) {
            if(event.getAuthor().isBot()) return;
            event.getAuthor().openPrivateChannel().queue((privateChannel -> privateChannel.sendMessage("Pardon our dust, this feature is currently under construction! " + "\uD83D\uDEE0️").queue()));
        }
    }
}
