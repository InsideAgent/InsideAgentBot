package dev.jacrispys.JavaBot.webhooks;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;

import java.io.IOException;
import java.io.InputStream;

public class SpotifyStats implements StatHooks<SpotifyStats> {

    private final TextChannel channel;
    private WebhookAction webhookAction;


    public SpotifyStats(TextChannel channel) {
        this.channel = channel;
        WebAgent webAgent = WebAgent.getInstance();
        webhookAction = webAgent.createWebAgent(channel).getWebHook();
    }

    @Override
    public Webhook build() {
        return webhookAction.complete();
    }

    public void sendMessage(Webhook webhook, String message) {
        JDAWebhookClient.from(webhook).send(message);
    }

    @Override
    public void getProfilePicture() {

    }

    @Override
    public SpotifyStats setName(String name) {
        webhookAction.setName(name).queue();
        return this;
    }

    @Override
    public SpotifyStats setProfilePicture() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/SpotifyIcon.png");
            Icon icon = Icon.from(inputStream, Icon.IconType.PNG);
            webhookAction.setAvatar(icon).queue();
        }catch (IOException ignored) {
            webhookAction.getChannel().sendMessage("Cannot find file for webhook!").queue();
            throw new NullPointerException("Could not find correct files for webhook!");
        }
        return this;
    }
}
