package com.jacrispys.JavaBot.Utils;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface StreamingOverride extends Activity {

    static Activity streaming(@Nonnull String name, @Nullable String url) {
        Checks.notEmpty(name, "Provided game name");
        name = Helpers.isBlank(name) ? name : name.trim();
        Checks.notLonger(name, 128, "Name");
        Activity.ActivityType type;
        type = ActivityType.STREAMING;

        return EntityBuilder.createActivity(name, url, type);
    }
}
