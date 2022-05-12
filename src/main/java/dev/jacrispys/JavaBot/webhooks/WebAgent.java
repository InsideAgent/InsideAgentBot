package dev.jacrispys.JavaBot.webhooks;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WebAgent {

    private static WebhookAction webAgent;
    public static String token;
    private static WebAgent INSTANCE;
    private WebAgent() {}


    @Nullable
    public WebhookAction getWebHook() {
        return webAgent != null ? webAgent : null;
    }

    public static WebAgent getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new WebAgent();
        }

        return INSTANCE;
    }

    public WebAgent createWebAgent(TextChannel channel) {
        if(webAgent != null) {
            channel.retrieveWebhooks().queue(webhooks -> {
                for(Webhook webhook : webhooks) {

                    if(Objects.equals(webhook.getToken(), token)) {
                        webhook.delete().queue();
                        webAgent = channel.createWebhook("WebAgent");
                        return;
                    }
                }
            });
        }
        webAgent = channel.createWebhook("WebAgent");

        return this;
    }

}
