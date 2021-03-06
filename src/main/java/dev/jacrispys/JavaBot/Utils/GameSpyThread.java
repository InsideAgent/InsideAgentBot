package dev.jacrispys.JavaBot.Utils;


import dev.jacrispys.JavaBot.Utils.MySQL.MySQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameSpyThread extends Thread {

    @SuppressWarnings("all")
    private final JDA jda;
    public Map<Guild, ScheduledExecutorService> runningSpies = new HashMap<>();
    private final MySQLConnection connection = MySQLConnection.getInstance();
    private final Logger logger = LoggerFactory.getLogger(GameSpyThread.class);


    public GameSpyThread(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run() {
        logger.info("{} - GameSpyThread successfully started!", GameSpyThread.class.getSimpleName());
    }

    private int spyCount = 0;

    public void addNewSpy(Guild guild) {
        runningSpies.put(guild, runSpy(guild));
        spyCount++;
        logger.info("Added New Spy [" + spyCount + "] - {}", GameSpyThread.class.getSimpleName());

    }


    @Deprecated
    public void setSpyData(Map<Guild, ScheduledExecutorService> guildList) {
        runningSpies = guildList;
    }

    @SuppressWarnings("unused")
    public Map<Guild, ScheduledExecutorService> getRunningSpies() {
        return runningSpies;
    }

    protected ScheduledExecutorService runSpy(Guild guild) {
       ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
       Runnable service = () -> {
           HashMap<Member, Activity> guildData = fetchData(guild);
           for(Member member : guildData.keySet()) {
               try {
                   long currentTime;
                   ResultSet rs = connection.queryCommand("SELECT totalTime FROM inside_agent_bot.gamespyusers WHERE memberId=" + member.getIdLong()
                           + " AND Guild=" + guild.getId());
                   if(!rs.next()) {
                       continue;
                   }
                   rs.beforeFirst();
                   rs.next();
                   currentTime = rs.getInt("totalTime");

                   currentTime = currentTime + 5;
                   rs.close();
               connection.executeUpdate("UPDATE inside_agent_bot.gamespyusers SET totalTime=" + currentTime +
                       " WHERE Guild=" + guild.getId() + " AND MemberId=" + member.getIdLong());
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }

       };
       executorService.scheduleAtFixedRate(service, 1, 5, TimeUnit.SECONDS);
       return executorService;

    }

    public ScheduledExecutorService getSpy(Guild guild) {
        return runningSpies.get(guild);
    }

    protected HashMap<Member, Activity> fetchData(Guild guild) {
        HashMap<Member, Activity> dataMap = new HashMap<>();
        for (Member member : guild.getMembers()) {
            for (Activity activity : member.getActivities()) {
                if (activity.getTimestamps() != null) {
                    dataMap.put(member, activity);
                }
            }
        }
        return dataMap;
    }

    protected Map<Member, Long> getTotalTime(Guild guild) throws Exception {
        try {
            Map<Member, Long> timeMap = new HashMap<>();
            ResultSet rs = connection.queryCommand("SELECT * FROM inside_agent_bot.gamespyusers WHERE Guild=" + guild.getId() + " AND totalTime!=0 " +
                    "ORDER BY totalTime DESC;");
            rs.beforeFirst();
            while(rs.next()) {
                long memberId = rs.getLong("MemberId");
                int timeSeconds = rs.getInt("totalTime");
                timeMap.put(guild.getMemberById(memberId), (long) timeSeconds);
            }
            return timeMap;
        } catch(Exception ex) {
            throw new Exception("Could not locate TimeData!");
        }
    }


    public void sendUpdate(Guild guild) {

        TextChannel gameSpyChannel;
        try {
            gameSpyChannel = guild.getTextChannelById(connection.getGameSpyChannel(guild));
        } catch (Exception e) {
            System.out.println("Could not get gamespy channel for guild: " + guild.getName());
            return;
        }

        assert gameSpyChannel != null;
        gameSpyChannel.sendMessage("Currently Crunching numbers from: " + guild.getName()).queue();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("GameSpy??? Report");
        embedBuilder.setColor(new Color(0x530C84));
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyy 'at' hh:mma");
        Date date = new Date();
        embedBuilder.setDescription("Generated by IAB on: " + formatter.format(date) +"\n");
        embedBuilder.setAuthor("Made & Maintained by: Jacrispys");
        embedBuilder.setImage("https://i.imgur.com/MD8RHHL.png");

        StringBuilder richValue = new StringBuilder();
        for (Member member : guild.getMembers()) {
            for (Activity activity : member.getActivities()) {
                String time;
                if (activity.getTimestamps() != null) {
                    long secs = activity.getTimestamps().getElapsedTime(ChronoUnit.SECONDS);
                    time = String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
                    richValue.append("\n").append(member.getAsMention()).append(" - ").append(activity.getName()).append(" for ").append(time);
                }
            }
        }

        if(richValue.toString().length() >= 1024 && richValue.toString().length() <= 2048) {
            embedBuilder.addField("*Games*", richValue.substring(0,1024), false);
            embedBuilder.addField("*Games (Cont)*", richValue.substring(1024,richValue.toString().length()), false);
        } else {
            embedBuilder.addField("*Games*", richValue + "\n", false);
        }

        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Member member : getTotalTime(guild).keySet()) {
                Map<Member, Long> dataMap = getTotalTime(guild);
                long seconds = dataMap.get(member);
                String time = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
                stringBuilder.append(member.getAsMention()).append(" played for: ").append(time).append("!\n");
            }
            embedBuilder.addField("Playtime this Week!", stringBuilder.toString(), false);
        }catch (Exception ex) {
            return;
        }

        gameSpyChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
