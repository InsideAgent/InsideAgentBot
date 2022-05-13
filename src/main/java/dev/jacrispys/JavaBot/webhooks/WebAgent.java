package dev.jacrispys.JavaBot.webhooks;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.jetbrains.annotations.Nullable;

public class WebAgent {

    private static Webhook webAgent;
    private static WebAgent INSTANCE;

    private WebAgent() {
    }


    @Nullable
    public Webhook getWebHook() {
        return webAgent;
    }

    public static WebAgent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WebAgent();
        }

        return INSTANCE;
    }

    public WebAgent createWebAgent(TextChannel channel) {
        if (webAgent != null) {
            channel.getGuild().retrieveWebhooks().queue(webhooks -> webhooks.forEach(webhook -> {
                if (webhook.getOwner() == channel.getGuild().getSelfMember()) {
                    webAgent = webhook;
                }
            }));
            return this;
        }
        webAgent = channel.createWebhook("WebAgent").complete();

        return this;
    }

}
