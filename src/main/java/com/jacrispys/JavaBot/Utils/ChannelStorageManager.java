package com.jacrispys.JavaBot.Utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record ChannelStorageManager(Yaml yaml) {
    public static ChannelStorageManager INSTANCE;

    public ChannelStorageManager(Yaml yaml) {
        this.yaml = yaml;
        INSTANCE = this;
    }

    public static ChannelStorageManager getInstance() {
        return INSTANCE;
    }

    private  InputStream loadConfig() {
        return getClass().getClassLoader().getResourceAsStream("guildData.yml");
    }

    /**
     * @param guild is the guild to be added to the config
     * @return true if the guild was successfully added,
     * will {@return} false if the guild was already located in the data file
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

    public void addGuildData(Guild guild, String dataTag, Object data) {
        try {
            Map<Long, Map<String, Object>> guildList = this.yaml.load(loadConfig());
            if ((guildList.containsKey(Long.parseLong(guild.getId())))) {
                Map<String, Object> guildData = guildList.get(Long.parseLong(guild.getId()));
                URL resourceUrl = getClass().getClassLoader().getResource("guildData.yml");
                File file = new File(resourceUrl.toURI());
                FileWriter writer = new FileWriter(file);
                guildData.putIfAbsent(dataTag, data);
                yaml.dump(guildList, writer);
                System.out.println(writer);
                return;
            }
            System.out.println(guildList);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setGuildData(Guild guild, String dataTag, Object data) {
        try {
            Map<Long, Map<String, Object>> guildList = this.yaml.load(loadConfig());
            if ((guildList.containsKey(Long.parseLong(guild.getId())))) {
                Map<String, Object> guildData = guildList.get(Long.parseLong(guild.getId()));
                URL resourceUrl = getClass().getClassLoader().getResource("guildData.yml");
                File file = new File(resourceUrl.toURI());
                FileWriter writer = new FileWriter(file);
                guildData.put(dataTag, data);
                yaml.dump(guildList, writer);
                System.out.println(writer);
                return;
            }
            System.out.println(guildList);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public Object getGuildData(Guild guild, String dataTag) throws NullPointerException {
        try {
            InputStream is;
            is = getClass().getClassLoader().getResourceAsStream("guildData.yml");
            Map<Long, Map<String, Object>> values = yaml.load(is);
            if (values.containsKey(Long.parseLong(Objects.requireNonNull(guild.getId())))) {
                Map<String, Object> guildData = values.get(Long.parseLong(guild.getId()));
                return guildData.get(dataTag);
            }
            throw new NullPointerException("Could not locate the queried data!");
        } catch (Exception ex) {
            throw new NullPointerException("Could not locate the queried data!");
        }
    }


}
