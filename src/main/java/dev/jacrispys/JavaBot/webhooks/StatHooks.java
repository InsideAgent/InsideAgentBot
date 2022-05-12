package dev.jacrispys.JavaBot.webhooks;

import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;

public interface StatHooks<T> {

    public void getProfilePicture();

    SpotifyStats setName(String name);

    public T setProfilePicture();
    public Webhook build();
}
