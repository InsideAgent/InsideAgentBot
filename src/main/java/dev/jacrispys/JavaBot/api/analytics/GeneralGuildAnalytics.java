package dev.jacrispys.JavaBot.api.analytics;

import dev.jacrispys.JavaBot.api.analytics.objects.AudioUser;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildStats;
import dev.jacrispys.JavaBot.api.analytics.objects.GuildUser;
import dev.jacrispys.JavaBot.api.analytics.objects.Stats;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Interface for analytics that are for general statistics specific to a guild, but not to audio.
 * <br> Child of {@link Stats}
 */
public interface GeneralGuildAnalytics extends Stats {

    OffsetDateTime getJoinDate();

    List<GuildUser> getBotUsers();

    List<GuildUser> getRegularUsers(int frequency);

    GuildStats getOverallStats();


    GuildUser getGuildUser(User user);

    Guild getParentGuild();


}
