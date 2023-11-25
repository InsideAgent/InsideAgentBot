package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.JavaBotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelpCommand extends ListenerAdapter {
    // TODO: 11/24/2023 Implement along with stats commands.

    public HelpCommand() {

    }

    public List<CommandData> updateJdaCommands() {
        List<CommandData> commands = new ArrayList<>();
        Collections.addAll(commands,
                Commands.slash("help", "Provide a drop down list of categories to receive help for."));

        return commands;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("help")) {
            MessageCreateBuilder mcb = new MessageCreateBuilder();
            mcb.addActionRow(generateHelpMenu());
            event.reply(mcb.build()).setEphemeral(true).queue();
        }
    }

    private SelectMenu generateHelpMenu() {
        return StringSelectMenu.create("helpmenu")
                .addOption("Music Commands", "music", "Help information for the various music commands.", Emoji.fromUnicode("\uD83C\uDFB6"))
                .addOption("Embed CLI", "embed", "Help information for the embed builder commands.", Emoji.fromUnicode("\uD83C\uDF9B️"))
                .addOption("Stats Commands", "stats", "Help information for the various stats commands.", Emoji.fromUnicode("\uD83D\uDCCA"))
                .addOption("Information & Credits", "info", "Show information about the bot & its developer.", Emoji.fromUnicode("\uD83D\uDCE3"))
                .build();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equalsIgnoreCase("helpmenu")) {
            event.editSelectMenu(event.getSelectMenu().withDisabled(true)).queue();
            String selected = event.getSelectedOptions().get(0).getValue();
            switch (selected) {
                case "info" -> {
                    event.getInteraction().getHook().editOriginal("").queue();
                    event.getInteraction().getHook().editOriginalEmbeds(generateInfoEmbed(event.getJDA()).build()).queue();
                }
                case "music" ->  {
                    event.getInteraction().getHook().editOriginal("Please select a music command to receive information about.").queue();
                    event.editSelectMenu(musicCommandMenu().withDisabled(false)).queue();
                }
                case "embed" -> {
                    Button link = Button.link("https://github.com/InsideAgent/InsideAgentBot#embed-builder", "Click Here!");
                    event.getInteraction().getHook().editOriginal("For more information about the embed builder, click the button below.").setActionRow(link).queue();
                }
                case "stats" -> {
                    event.getInteraction().getHook().editOriginal("Currently not available! Please check back soon.").queue();
                    event.getInteraction().editSelectMenu(event.getSelectMenu().withDisabled(false)).queue();
                }
            }
        }

        if (event.getComponentId().equalsIgnoreCase("musicmenu")) {
            event.deferReply(true).queue();
            String selected = event.getSelectedOptions().get(0).getValue();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Unknown Command!");
            switch (selected) {
                case "play" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Search query, or link to track", OptionType.STRING);
                    Pair<String, OptionType> var2 = new ImmutablePair<>("Search provider, choose between Spotify/Apple/Youtube to use for the query.", OptionType.SUB_COMMAND);
                    eb = generateCommandInfo("Play", "Adds a song to the current queue." +
                            " Optional parameter to specify what provider to search for the track with.", event.getJDA(), var1, var2);
                }
                case "skip" -> eb = generateCommandInfo("Skip", "Skips the current playing track.", event.getJDA());
                case "volume" -> eb = generateCommandInfo("Volume", "Adjusts the volume of the player, any number 0-500.", event.getJDA());
                case "clear" -> eb = generateCommandInfo("Clear", "Clears the current queue, does not stop the current playing track.", event.getJDA());
                case "stop" -> eb = generateCommandInfo("Stop/Pause", "Pauses the player, leaves the queue and current playing track intact.", event.getJDA());
                case "resume" -> eb = generateCommandInfo("Resume", "Resumes the current playing track if the player is currently paused.", event.getJDA());
                case "disconnect" -> eb = generateCommandInfo("Disconnect/DC", "Removes the bot from the current voice channel, also clears the queue (including current playing track).", event.getJDA());
                case "follow" -> eb = generateCommandInfo("Follow", "Moves the bot to your current voice channel.", event.getJDA());
                case "queue" -> eb = generateCommandInfo("Queue", "Shows an embed with the current track queue.", event.getJDA());
                case "shuffle" -> eb = generateCommandInfo("Shuffle", "Takes the current queue and randomizes the order.", event.getJDA());
                case "song" -> eb = generateCommandInfo("Song/Info/Song-Info", "Displays info about the current playing track. Including remaining duration, source link, and more.", event.getJDA());
                case "remove" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Track index within current queue.", OptionType.INTEGER);
                    eb = generateCommandInfo("Remove", "Finds the track at a given `index` and removes it from the queue" ,
                            event.getJDA(), var1);
                } 
                case "seek" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Minutes of the song to seek to.", OptionType.INTEGER);
                    Pair<String, OptionType> var2 = new ImmutablePair<>("Seconds of the song to seek to.", OptionType.INTEGER);
                    Pair<String, OptionType> var3 = new ImmutablePair<>("(Optional) Hours of the song to seek to.", OptionType.INTEGER);
                    eb = generateCommandInfo("Seek", "Accepts two numbers for `minutes`, and `seconds` to seek to within the current playing track. Also has a optional hours input." ,
                            event.getJDA(), var1, var2, var3);
                }
                case "fix" -> eb = generateCommandInfo("Fix", "Silently reconnects all users to the voice channel, utilizing the servers from the best negotiated region.", event.getJDA());
                case "loop" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Type of loop to set. Queue or Song.", OptionType.SUB_COMMAND);
                    eb = generateCommandInfo("Loop", "Loops songs that play. Accepts a optional parameter to specify the whole queue, or just the current playing track. (Default: queue)", event.getJDA(), var1);
                }
                case "move" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("First index to swap.", OptionType.INTEGER);
                    Pair<String, OptionType> var2 = new ImmutablePair<>("Second index to swap.", OptionType.INTEGER);
                    eb = generateCommandInfo("Move", "Takes in two `indexes` from the queue, and swaps their positions in the queue.", event.getJDA(), var1, var2);
                }
                case "hijack" -> eb = generateCommandInfo("Hijack", "Secret command, only available to `Super Users`. Allows for live music mixing.", event.getJDA());
                case "playtop" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Search query, or link to track", OptionType.STRING);
                    Pair<String, OptionType> var2 = new ImmutablePair<>("Search provider, choose between Spotify/Apple/Youtube to use for the query.", OptionType.SUB_COMMAND);
                    eb = generateCommandInfo("Playtop", "Adds a song to the top of the current queue." +
                            " Optional parameter to specify what provider to search for the track with.", event.getJDA(), var1, var2);
                }
                case "skipto" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Track index within current queue.", OptionType.INTEGER);
                    eb = generateCommandInfo("Skipto", "Removes all tracks ahead of the provided index.",
                            event.getJDA(), var1);
                }
                case "fileplay" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("File to add to the queue", OptionType.ATTACHMENT);
                    eb = generateCommandInfo("Fileplay", "Adds a song to the current queue." +
                            " Rather than searching for the song this command directly plays the attachment if possible.", event.getJDA(), var1);
                }
                case "radio" -> {
                    Pair<String, OptionType> var1 = new ImmutablePair<>("Limit: Max amount of songs to be generated.", OptionType.INTEGER);
                    Pair<String, OptionType> var2 = new ImmutablePair<>("Popularity: number 0-100 of maximum popularity that a track can have to be eligible.", OptionType.INTEGER);
                    eb = generateCommandInfo("Radio", "Allows the user to choose up to 5 genres to generate a queue from.", event.getJDA(), var1, var2);
                }
                
            }
            event.getInteraction().getHook().editOriginalEmbeds(eb.build()).queue();
        }

    }

    private StringSelectMenu musicCommandMenu() {

        return StringSelectMenu.create("musicmenu")
                .addOption("Play", "play", "The /play command", Emoji.fromUnicode("▶️"))
                .addOption("Skip", "skip", "The /skip command", Emoji.fromUnicode("⏩"))
                .addOption("Volume", "volume", "The /volume command", Emoji.fromUnicode("\uD83D\uDD0A"))
                .addOption("Clear", "clear", "The /clear command", Emoji.fromUnicode("❌"))
                .addOption("Stop/Pause", "stop", "The /stop command", Emoji.fromUnicode("⏹️"))
                .addOption("Resume", "resume", "The /resume command", Emoji.fromUnicode("⏯️"))
                .addOption("Disconnect/DC", "disconnect", "The /disconnect and /dc command", Emoji.fromUnicode("\uD83D\uDC4B"))
                .addOption("Follow", "follow", "The /follow command", Emoji.fromUnicode("✈️"))
                .addOption("Queue", "queue", "The /queue command", Emoji.fromUnicode("\uD83D\uDCDC"))
                .addOption("Shuffle", "shuffle", "The /shuffle command", Emoji.fromUnicode("\uD83D\uDD00"))
                .addOption("Song/Info", "song", "The /song and /info command", Emoji.fromUnicode("ℹ️"))
                .addOption("Remove", "remove", "The /remove command", Emoji.fromUnicode("⛔"))
                .addOption("Seek", "seek", "The /seek command", Emoji.fromUnicode("\uD83D\uDD0D"))
                .addOption("Fix", "fix", "The /fix command", Emoji.fromUnicode("\uD83D\uDEE0️"))
                .addOption("Loop", "loop", "The /loop command", Emoji.fromUnicode("\uD83D\uDD01"))
                .addOption("Move", "move", "The /move command", Emoji.fromUnicode("\uD83D\uDD04"))
                .addOption("Hijack", "hijack", "The /hijack command", Emoji.fromUnicode("\uD83D\uDE0E"))
                .addOption("Playtop", "playtop", "The /playtop command", Emoji.fromUnicode("\uD83D\uDD1D"))
                .addOption("Skipto", "skipto", "The /skipto command", Emoji.fromUnicode("⏭️"))
                .addOption("Fileplay", "fileplay", "The /fileplay command", Emoji.fromUnicode("\uD83D\uDCC1"))
                .addOption("Radio", "radio", "The /radio command", Emoji.fromUnicode("\uD83C\uDF99️"))
                .build();
    }


    @SafeVarargs
    private EmbedBuilder generateCommandInfo(String name, String description, JDA jda, Pair<String, OptionType>... params) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(name + " command information");
        eb.setThumbnail(jda.getSelfUser().getEffectiveAvatarUrl());
        eb.addField("Description: ", description, true);
        StringBuilder sb = new StringBuilder();
        if (params.length == 0) {
            sb.append("N/A");
        }
        for (Pair<String, OptionType> param : params) {
            sb.append(param.getRight().name()).append(" - `").append(param.getLeft()).append("`\n");
        }
        eb.addField("Parameters (Options): ", sb.toString(), true);
        eb.setColor(0x4662a9);

        return eb;
    }

    private EmbedBuilder generateInfoEmbed(JDA jda) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Inside Agent Information");
        User me = jda.getUserById(731364923120025705L);
        String gitPfp = "https://avatars.githubusercontent.com/u/69219325";
        assert me != null;
        eb.setAuthor("Created by: Jacrispys", "https://github.com/Jacrispys", me.getEffectiveAvatarUrl());
        eb.setThumbnail(gitPfp);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Inside Agent v" + JavaBotMain.VERSION, "https://i.imgur.com/CqqBUbF.png");
        eb.addField("Why: ", "I created this project for my friends after rhythm bot was shut down. Little did I know this would be one " +
                "of the largest projects I have ever took on. As well as broadening my knowledge in programming 100 fold.", false);
        eb.addField("How you can help: ", """
                If you would like to help me and my passion for coding, a simple [star](https://github.com/InsideAgent/InsideAgentBot) goes a long ways!

                 If your a developer looking to contribute to the project click [here](https://github.com/InsideAgent/InsideAgentBot#contributing).

                If you would like to report a bug/issue or suggest a feature please click [here](https://github.com/InsideAgent/InsideAgentBot/issues).""", false);
        float r, g, b;
        long t = System.currentTimeMillis();
        r = (float) Math.abs(Math.sin(t));
        g = (float) Math.abs(Math.sin(t + 0.33f * 2.0f * Math.PI));
        b = (float) Math.abs(Math.sin(t + 0.66f * 2.0f * Math.PI));
        eb.setColor(new Color((r * r), (g * g), b * b));

        return eb;
    }
}
