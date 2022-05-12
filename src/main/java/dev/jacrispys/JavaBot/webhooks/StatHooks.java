package dev.jacrispys.JavaBot.webhooks;

import net.dv8tion.jda.api.entities.Webhook;

public interface StatHooks<T> {

    public void getProfilePicture();
    public T setProfilePicture();
    public void genHook(String message);
}
