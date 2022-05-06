
package dev.jacrispys.JavaBot.Events;

import dev.jacrispys.JavaBot.Utils.BlockedWords;
import dev.jacrispys.JavaBot.Utils.MessageFilter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class WordBlocker extends ListenerAdapter {
/*
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        for(BlockedWords blocked : BlockedWords.values()) {
            ArrayList<String> blockedWords = blocked.getList();
            String message = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
            if(MessageFilter.filterMessage(message, blockedWords) != null) {
                String wordBlocked = MessageFilter.filterMessage(message, blockedWords);
                event.getMessage().reply("AYO?!? You really gonna say \"" + wordBlocked + "\" like its nothin??").queue();
            }
        }
    }
*/
}

