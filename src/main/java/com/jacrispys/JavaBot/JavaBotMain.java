package com.jacrispys.JavaBot;

import com.jacrispys.JavaBot.Commands.ComplaintCommand;
import com.jacrispys.JavaBot.Commands.PrivateMessageCommands.DefaultPrivateMessageResponse;
import com.jacrispys.JavaBot.Commands.RegisterGuildCommand;
import com.jacrispys.JavaBot.Utils.ChannelStorageManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.yaml.snakeyaml.Yaml;


public class JavaBotMain {

    public static void main(String[] args) throws Exception {
        JDA jda = JDABuilder.createDefault("Nzg2NzIxNzU1NTYwODA0Mzcz.X9Khuw.Y0pgvYATjsNpAKzRMwEeXPnGsi8").build();

        jda.getPresence().setActivity(Activity.streaming("Well this is a thing now...", "https://www.twitch.tv/jacrispyslive"));
        jda.addEventListener(new DefaultPrivateMessageResponse());
        jda.addEventListener(new ComplaintCommand());
        jda.addEventListener(new RegisterGuildCommand());
        new ChannelStorageManager(new Yaml());


    }
}
