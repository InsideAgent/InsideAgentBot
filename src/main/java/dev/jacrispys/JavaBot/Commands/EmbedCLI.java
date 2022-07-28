package dev.jacrispys.JavaBot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmbedCLI extends ListenerAdapter {

    private static EmbedCLI instance = null;
    private static final Map<String, Channel> buttonIds = new HashMap<>();
    private final Map<String, Message> messageId = new HashMap<>();

    public static void addEmbedCLI(Channel channel, String buttonId) {
        buttonIds.put(buttonId, channel);
    }

    public static EmbedCLI getInstance() {
        if (instance == null) {
            instance = new EmbedCLI();
        }
        return instance;
    }

    private EmbedCLI() {
    }

    protected Message generateEmbedMessage(String buttonId) {
        MessageBuilder message = new MessageBuilder();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Example Title");
        builder.addField("", "To be sent in: " + buttonIds.get(buttonId.replace("builder:", "")).getAsMention(), false);
        message.setEmbeds(builder.build());
        List<ActionRow> components = new ArrayList<>();
        components.add(ActionRow.of(
                SelectMenu.create("select:" + buttonId.replace("builder:", ""))
                        .addOption("Title", "title", "Set the Embed Title")
                        .addOption("Color", "color", "Set the embed color!")
                        .addOption("Add Field", "add", "Adds a new field!")
                        .addOption("Remove Field", "remove", "Removes a field!")
                        .build()));
        components.add(ActionRow.of(Button.success("success:" + buttonId.replace("builder:", ""), "Send Embed!")));
        message.setActionRows(components);
        return message.build();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (buttonIds.containsKey(event.getComponentId().replace("builder:", ""))) {
            event.deferReply(true).queue();
            event.getHook().editOriginal(generateEmbedMessage(event.getButton().getId())).queue();
        } else if (buttonIds.containsKey(event.getComponentId().replace("success:", ""))) {
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            TextChannel channel = (TextChannel) buttonIds.get(event.getComponentId().replace("success:", ""));
            channel.sendMessage(new MessageBuilder(event.getUser().getAsMention() + " sent embed:").setEmbeds(embed).build()).queue();
            event.editMessage(new MessageBuilder("\u200B").build()).queue();
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (buttonIds.containsKey(event.getComponentId().replace("select:", ""))) {
            String selection = event.getValues().get(0);
            if(selection.equalsIgnoreCase("add")) {
                TextInput header = TextInput.create("header", "Heading for Field", TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(1)
                        .setPlaceholder("header" + " for the embed.")
                        .build();
                TextInput body = TextInput.create("body", "Body of the field", TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(1)
                        .setPlaceholder("Body for the embed.")
                        .build();
                TextInput inline = TextInput.create("bool", "Boolean:True/False (Inline)", TextInputStyle.SHORT)
                        .setMaxLength(5)
                        .setMinLength(4)
                        .setPlaceholder("is it inline for the embed.")
                        .build();
                Modal modal = Modal.create(event.getComponentId().replace("select:", "modal:"), selection + " Input")
                        .addActionRows(ActionRow.of(header), ActionRow.of(body), ActionRow.of(inline))
                        .build();
                messageId.put(event.getComponentId().replace("select:", ""), event.getMessage());
                event.replyModal(modal).queue();
                return;
            }
            TextInput title = TextInput.create(selection, selection, TextInputStyle.SHORT)
                    .setMaxLength(200)
                    .setMinLength(1)
                    .setPlaceholder(selection + " for the embed.")
                    .build();
            Modal modal = Modal.create(event.getComponentId().replace("select:", "modal:"), selection + " Input")
                    .addActionRow(title)
                    .build();
            messageId.put(event.getComponentId().replace("select:", ""), event.getMessage());
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (buttonIds.containsKey(event.getModalId().replace("modal:", ""))) {
            Message message = messageId.get(event.getModalId().replace("modal:", ""));
            MessageEmbed embed = message.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed);
            ModalMapping value = event.getValues().get(0);
            switch (value.getId()) {
                case "title" -> event.editMessageEmbeds(builder.setTitle(event.getValue("title").getAsString()).build()).queue();
                case "color" -> {
                    try {
                        int color = Integer.decode(value.getAsString());
                        event.editMessageEmbeds(builder.setColor(color).build()).queue();
                    } catch (NumberFormatException ex) {
                        event.reply("Color must be formatted as '0xHEX' example: 0xffffff").setEphemeral(true).queue();
                    }
                }
                case "header","body","bool" -> {
                        boolean bool = Boolean.parseBoolean(event.getValue("bool").getAsString());
                        event.editMessageEmbeds(builder.addField(event.getValue("header").getAsString(), event.getValue("body").getAsString(), bool).build()).queue();

                }
                case "remove" -> {
                    try {
                        int index = Integer.parseInt(value.getAsString());
                        List<MessageEmbed.Field> fields = new ArrayList<>(builder.getFields());
                        fields.remove(index);
                        builder.clearFields();
                        fields.forEach(builder::addField);
                        event.editMessageEmbeds(builder.build()).queue();
                    } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                        event.reply("Must be an integer, and the index must exist!").setEphemeral(true).queue();
                    }
                }

            }
        }
    }
}
