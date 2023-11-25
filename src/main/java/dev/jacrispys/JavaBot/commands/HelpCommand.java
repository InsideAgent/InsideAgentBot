package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.JavaBotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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
        StringSelectMenu helpMenu = StringSelectMenu.create("helpmenu")
                .addOption("Music Commands", "music", "Help information for the various music commands.", Emoji.fromUnicode("\uD83C\uDFB6"))
                .addOption("Embed CLI", "embed", "Help information for the embed builder commands.", Emoji.fromUnicode("\uD83C\uDF9B\uFE0F"))
                .addOption("Stats Commands", "stats", "Help information for the various stats commands.", Emoji.fromUnicode("\uD83D\uDCCA"))
                .addOption("Information & Credits", "info", "Show information about the bot & its developer.", Emoji.fromUnicode("\uD83D\uDCE3"))
                .build();
        return helpMenu;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equalsIgnoreCase("helpmenu")) {
            event.editSelectMenu(event.getSelectMenu().withDisabled(true)).queue();
            String selected = event.getSelectedOptions().get(0).getValue();
            switch (selected) {
                case "info" ->
                        event.getInteraction().getHook().editOriginalEmbeds(generateInfoEmbed(event.getJDA()).build()).queue();
                case "music" -> event.editSelectMenu(musicCommandMenu().withDisabled(false)).queue();
            }
        }

    }

    private StringSelectMenu musicCommandMenu() {

        StringSelectMenu menu = StringSelectMenu.create("musicmenu")
                .build();
        return menu;
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
        eb.addField("How you can help: ", "If you would like to help me and my passion for coding, " +
                "a simple [star](https://github.com/InsideAgent/InsideAgentBot) goes a long ways!\n\n " +
                "If your a developer looking to contribute to the project click [here](https://github.com/InsideAgent/InsideAgentBot#contributing).\n\n" +
                "If you would like to report a bug/issue or suggest a feature please click [here](https://github.com/InsideAgent/InsideAgentBot/issues).", false);
        float r, g, b;
        long t = System.currentTimeMillis();
        r = (float) Math.abs(Math.sin(t));
        g = (float) Math.abs(Math.sin(t + 0.33f * 2.0f * Math.PI));
        b = (float) Math.abs(Math.sin(t + 0.66f * 2.0f * Math.PI));
        eb.setColor(new Color((r * r), (g * g), b * b));

        return eb;
    }
}
