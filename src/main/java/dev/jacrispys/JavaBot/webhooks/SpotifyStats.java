package dev.jacrispys.JavaBot.webhooks;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.internal.entities.WebhookImpl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class SpotifyStats implements StatHooks<SpotifyStats> {

    private final Webhook webhook;
    private final WebhookManager webhookManager;
    private final TextChannel channel;
    private final WebhookClient<SpotifyStats> webhookClient;

    @SuppressWarnings("unchecked")
    public SpotifyStats(TextChannel channel, long id, WebhookType type) {
        this.webhook = new WebhookImpl(channel, id, type).setToken(UUID.randomUUID().toString());
        this.channel = channel;
        this.webhookClient = (WebhookClient<SpotifyStats>) webhook;
        this.webhookManager = (webhook).getManager();
    }

    @Override
    public void genHook(String message) {
        webhookClient.sendMessage(message).queue();
        webhook.delete().queue();
    }

    @Override
    public void getProfilePicture() {

    }

    @Override
    public SpotifyStats setProfilePicture() {
        try {
            File file = new File("C:\\Users\\jvanz\\Desktop\\Plugins\\DiscordBot\\Images\\Spotify_App_Logo.svg.png");
            webhookManager.setAvatar(Icon.from(file, Icon.IconType.PNG)).queue();
        } catch (IOException ignored) {
            channel.sendMessage("Could not set profile picture!").queue();
        }
        return this;
    }
}
