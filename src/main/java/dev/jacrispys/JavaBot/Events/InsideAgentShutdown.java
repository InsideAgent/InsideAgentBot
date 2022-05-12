package dev.jacrispys.JavaBot.Events;

import dev.jacrispys.JavaBot.webhooks.WebAgent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InsideAgentShutdown extends ListenerAdapter {

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        JDA jda = event.getJDA();
        for(Guild guild : jda.getGuilds()) {
            guild.getAudioManager().closeAudioConnection();
            guild.retrieveWebhooks().queue(webhooks -> webhooks.forEach(webhook -> {
                if(Objects.equals(webhook.getToken(), WebAgent.token)) {
                    webhook.delete().queue();
                }
            }));
        }
    }
}
