package dev.jacrispys.JavaBot.webhooks;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.managers.WebhookManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SpotifyStats implements StatHooks<SpotifyStats> {

    @SuppressWarnings("all")
    private final TextChannel channel;
    private final WebhookManager webhookManager;


    public SpotifyStats(TextChannel channel) {
        this.channel = channel;
        WebAgent webAgent = WebAgent.getInstance();
        webhookManager = webAgent.createWebAgent(channel).getWebHook().getManager();
        this.setIcon("images/SpotifyIcon.png");
    }

    @Override
    public Webhook build() {
        return webhookManager.getWebhook();
    }

    @Override
    public void sendMessage(Webhook webhook, String message) {
        JDAWebhookClient.from(webhook).send(message);
    }

    @Override
    public void sendMessageEmbed(Webhook webhook, MessageEmbed messageEmbed) {
        JDAWebhookClient.from(build()).send(messageEmbed);
    }


    @Override
    public SpotifyStats setName(String name) {
        webhookManager.setName(name).queue();
        return this;
    }


    @Override
    public SpotifyStats setIcon(URL url) {
        try {
            InputStream inputStream = url.openStream();
            Icon icon = Icon.from(inputStream, Icon.IconType.UNKNOWN);
            webhookManager.setAvatar(icon).queue();
        }catch (IOException ignored) {
            webhookManager.getChannel().sendMessage("Cannot find file for webhook!").queue();
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
            webhookManager.setAvatar(icon).queue();
        }catch (IOException ignored) {
            webhookManager.getChannel().sendMessage("Cannot find file for webhook!").queue();
            throw new NullPointerException("Could not find correct files for webhook!");
        }
        return this;
    }
}
