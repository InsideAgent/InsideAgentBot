package dev.jacrispys.JavaBot.webhooks;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SpotifyStats implements StatHooks {

    private final TextChannel channel;
    private final WebhookAction webhookAction;


    public SpotifyStats(TextChannel channel) {
        this.channel = channel;
        WebAgent webAgent = WebAgent.getInstance();
        webhookAction = webAgent.createWebAgent(channel).getWebHook();
    }

    @Override
    public Webhook build() {
        Webhook completed = webhookAction.complete();
        WebAgent.getInstance();
        WebAgent.token = completed.getToken();
        return completed;
    }

    @Override
    public void sendMessage(Webhook webhook, String message) {
        JDAWebhookClient.from(webhook).send(message);
    }

    @Override
    public void sendMessageEmbed(Webhook webhook, MessageEmbed messageEmbed) {
        JDAWebhookClient.from(webhook).send(messageEmbed);
    }


    @Override
    public SpotifyStats setName(String name) {
        webhookAction.setName(name).queue();
        return this;
    }


    @Override
    public SpotifyStats setIcon(URL url) {
        try {
            InputStream inputStream = url.openStream();
            Icon icon = Icon.from(inputStream, Icon.IconType.UNKNOWN);
            webhookAction.setAvatar(icon).queue();
        }catch (IOException ignored) {
            webhookAction.getChannel().sendMessage("Cannot find file for webhook!").queue();
            throw new NullPointerException("Could not find correct files for webhook!");
        }
        return this;
    }
    @Override
    public SpotifyStats setIcon(String filePath) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            assert inputStream != null;
            Icon icon = Icon.from(inputStream, Icon.IconType.UNKNOWN);
            webhookAction.setAvatar(icon).queue();
        }catch (IOException ignored) {
            webhookAction.getChannel().sendMessage("Cannot find file for webhook!").queue();
            throw new NullPointerException("Could not find correct files for webhook!");
        }
        return this;
    }
}
