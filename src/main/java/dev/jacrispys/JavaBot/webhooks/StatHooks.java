package dev.jacrispys.JavaBot.webhooks;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;

import java.net.URL;

public interface StatHooks{

    SpotifyStats setName(String name);

    Webhook build();

    SpotifyStats setIcon(URL url);

    SpotifyStats setIcon(String filePath);

    void sendMessageEmbed(Webhook webhook, MessageEmbed messageEmbed);

    void sendMessage(Webhook webhook, String message);
}
