package dev.jacrispys.JavaBot.webhooks;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;

import java.net.URL;

@Deprecated(forRemoval = true)
public interface StatHooks<T>{

    T setName(String name);

    Webhook build();

    T setIcon(URL url);

    T setIcon(String filePath);

    /**
     * @param webhook to send embed from
     * @param messageEmbed instance of embed to send
     */
    void sendMessageEmbed(Webhook webhook, MessageEmbed messageEmbed);

    void sendMessage(Webhook webhook, String message);
}
