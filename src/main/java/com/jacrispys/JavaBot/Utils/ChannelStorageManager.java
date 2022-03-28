package com.jacrispys.JavaBot.Utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ChannelStorageManager {
    private final Yaml yaml;

    public static ChannelStorageManager INSTANCE;

    public ChannelStorageManager(Yaml yaml) {
        this.yaml = yaml;
        INSTANCE = this;
    }

    public static ChannelStorageManager getInstance() { return INSTANCE; }

    protected InputStream loadConfig() {
        return getClass().getClassLoader().getResourceAsStream("guildData.yml");
    }

    /**
     *
     * @param guild is the guild to be added to the config
     * @return true if the guild was successfully added,
     * will {@return} false if the guild was already located in the data file
     *
     */
    public boolean addNewGuild(Guild guild) {
        try {
            Map<Long, Map<String, Long>> guildList = this.yaml.load(loadConfig());
            if (!(guildList.containsKey(Long.parseLong(guild.getId())))) {
                Map<String, Long> guildData = new HashMap<>();
                guildData.put("default", 0L);
                guildList.put(Long.parseLong(guild.getId()), guildData);
                URL resourceUrl = getClass().getClassLoader().getResource("guildData.yml");
                File file = new File(resourceUrl.toURI());
                FileWriter writer = new FileWriter(file);
                yaml.dump(guildList, writer);
                System.out.println(writer);
                return true;
            }
            System.out.println(guildList);
            return false;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setTicketChannel(Guild guild, TextChannel channel) {
        try {
            Map<Long, Map<String, Long>> guildList = this.yaml.load(loadConfig());
            if ((guildList.containsKey(Long.parseLong(guild.getId())))) {
                Map<String, Long> guildData = guildList.get(Long.parseLong(guild.getId()));
                URL resourceUrl = getClass().getClassLoader().getResource("guildData.yml");
                File file = new File(resourceUrl.toURI());
                FileWriter writer = new FileWriter(file);
                guildData.putIfAbsent("tickets", channel.getIdLong());
                yaml.dump(guildList, writer);
                System.out.println(writer);
                return true;
            }
            System.out.println(guildList);
            return false;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }



}
