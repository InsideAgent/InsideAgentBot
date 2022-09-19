package dev.jacrispys.JavaBot.commands;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmbedCLI extends ListenerAdapter {

    private static EmbedCLI instance = null;
    private static final Map<String, Channel> buttonIds = new HashMap<>();
    private final Map<String, Message> messageId = new HashMap<>();

    public void addEmbedCLI(Channel channel, String buttonId) {
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
        MessageBuilder message = new MessageBuilder("To be sent in: " + buttonIds.get(buttonId.replace("builder:", "")).getAsMention());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Example Title");
        message.setEmbeds(builder.build());
        List<ActionRow> components = new ArrayList<>();
        components.add(ActionRow.of(
                SelectMenu.create("select:" + buttonId.replace("builder:", ""))
                        .addOption("Title", "title", "Set the Embed Title!")
                        .addOption("Color", "color", "Set the embed color!")
                        .addOption("Add Field", "add", "Adds a new field!")
                        .addOption("Remove Field", "remove", "Removes a field!")
                        .addOption("Clear All Fields", "clear-fields", "Removes ALL Fields from the Embed!")
                        .addOption("Set Footer", "footer", "Sets the footer of the embed!")
                        .addOption("Clear Footer", "clear_footer", "Removes the current footer!")
                        .addOption("Set Author", "author", "Sets the author of the Embed!")
                        .addOption("Clear Author", "clear-author", "Removes the author from the embed!")
                        .addOption("Set Image", "image", "Sets the image for the thumbnail!")
                        .addOption("Clear Image", "clear-image", "Clears the Image of the Embed!")
                        .addOption("Set Thumbnail", "thumbnail", "Sets the Embed thumbnail!")
                        .addOption("Clear Thumbnail", "clear-thumbnail", "Removes the current thumbnail!")
                        .addOption("Set Description", "desc", "Sets the description for the embed!")
                        .addOption("Clear Description", "clear-desc", "Removes the description of the Embed!")
                        .build()));
        components.add(ActionRow.of(Button.success("success:" + buttonId.replace("builder:", ""), "Send Embed!")));
        message.setActionRows(components);
        return message.build();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (buttonIds.containsKey(event.getComponentId().replace("builder:", ""))) {
            event.getInteraction().editMessage("Please use `/embedbuilder` again to reuse this command!").queue();
            // event.deferReply(true).queue();
            event.getHook().editOriginal(generateEmbedMessage(event.getComponentId())).queue();
        } else if (buttonIds.containsKey(event.getComponentId().replace("success:", ""))) {
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            TextChannel channel = (TextChannel) buttonIds.get(event.getComponentId().replace("success:", ""));
            channel.sendMessage(new MessageBuilder(event.getUser().getAsMention() + " sent embed:").setEmbeds(embed).build()).queue();
            event.editMessage(new MessageBuilder("\u200B").build()).queue();
            buttonIds.remove(event.getComponentId().replace("success", ""));
            messageId.remove(event.getComponentId().replace("success", ""));
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (buttonIds.containsKey(event.getComponentId().replace("select:", ""))) {
            String selection = event.getValues().get(0);
            if (selection.equalsIgnoreCase("add")) {
                TextInput header = TextInput.create("header", "Heading for Field", TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(1)
                        .setPlaceholder("header" + " for the embed.")
                        .setRequired(false)
                        .build();
                TextInput body = TextInput.create("body", "Body of the field", TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(1)
                        .setPlaceholder("Body for the embed.")
                        .setRequired(false)
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
            } else if (selection.equalsIgnoreCase("footer")) {
                TextInput text = TextInput.create("foottext", "Text for the footer.", TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(1)
                        .setPlaceholder("Footer text.")
                        .build();
                TextInput url = TextInput.create("url", "Icon URL (Optional)", TextInputStyle.SHORT)
                        .setMaxLength(100)
                        .setMinLength(0)
                        .setPlaceholder("Icon for the footer.")
                        .setRequired(false)
                        .build();
                Modal modal = Modal.create(event.getComponentId().replace("select:", "modal:"), selection + " Input")
                        .addActionRows(ActionRow.of(text), ActionRow.of(url))
                        .build();
                messageId.put(event.getComponentId().replace("select:", ""), event.getMessage());
                event.replyModal(modal).queue();
                return;
            } else if (selection.equalsIgnoreCase("clear_footer")) {
                EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                event.editMessageEmbeds(builder.setFooter(null).build()).queue();
                return;
            } else if (selection.equalsIgnoreCase("author")) {
                TextInput text = TextInput.create("author-text", "Text for the Author.", TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(1)
                        .setPlaceholder("Author Name/Text.")
                        .build();
                TextInput url = TextInput.create("author-url", "URL (Optional)", TextInputStyle.SHORT)
                        .setMaxLength(100)
                        .setMinLength(0)
                        .setPlaceholder("Sets the link for the Author")
                        .setRequired(false)
                        .build();
                TextInput iconUrl = TextInput.create("icon-url", "Icon URL (Optional)", TextInputStyle.SHORT)
                        .setMaxLength(100)
                        .setMinLength(0)
                        .setPlaceholder("Sets the Icon of the Author")
                        .setRequired(false)
                        .build();
                Modal modal = Modal.create(event.getComponentId().replace("select:", "modal:"), selection + " Input")
                        .addActionRows(ActionRow.of(text), ActionRow.of(url), ActionRow.of(iconUrl))
                        .build();
                messageId.put(event.getComponentId().replace("select:", ""), event.getMessage());
                event.replyModal(modal).queue();
                return;
            } else if (selection.equalsIgnoreCase("clear-author")) {
                EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                event.editMessageEmbeds(builder.setAuthor(null).build()).queue();
                return;
            } else if (selection.equalsIgnoreCase("clear-fields")) {
                EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                event.editMessageEmbeds(builder.clearFields().build()).queue();
                return;
            } else if (selection.equalsIgnoreCase("clear-image")) {
                EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                event.editMessageEmbeds(builder.setImage(null).build()).queue();
                return;
            } else if (selection.equalsIgnoreCase("clear-thumbnail")) {
                EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                event.editMessageEmbeds(builder.setThumbnail(null).build()).queue();
                return;
            } else if (selection.equalsIgnoreCase("desc")) {
                TextInput desc = TextInput.create(selection, selection, TextInputStyle.PARAGRAPH)
                        .setMaxLength(4000)
                        .setMinLength(1)
                        .setPlaceholder("Description for the embed.")
                        .build();
                Modal modal = Modal.create(event.getComponentId().replace("select:", "modal:"), selection + " Input")
                        .addActionRow(desc)
                        .build();
                messageId.put(event.getComponentId().replace("select:", ""), event.getMessage());
                event.replyModal(modal).queue();
                return;
            } else if (selection.equalsIgnoreCase("clear-desc")) {
                EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
                event.editMessageEmbeds(builder.setDescription(null).build()).queue();
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

    @SuppressWarnings("all")
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
                case "header", "body", "bool" -> {
                    boolean bool = Boolean.parseBoolean(event.getValue("bool").getAsString());
                    event.editMessageEmbeds(builder.addField((event.getValue("header") == null ? null : event.getValue("header").getAsString()), (event.getValue("body") == null ? null : event.getValue("body").getAsString()), bool).build()).queue();

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
                case "footer", "foottext", "url" -> {
                    URL url = null;
                    if (event.getValue("url") != null) {
                        try {
                            url = new URL(event.getValue("url").getAsString());
                        } catch (MalformedURLException ex) {
                            event.reply("Invalid URL! Please try again!").setEphemeral(true).queue();
                        }
                    }
                    String foot = event.getValue("foottext").getAsString();
                    if (url != null) {
                        builder.setFooter(foot, url.toString());
                    } else {
                        builder.setFooter(foot);
                    }
                    event.editMessageEmbeds(builder.build()).queue();
                }
                case "icon-url", "author-text", "author-url" -> {
                    URL icon = null;
                    URL author = null;
                    if (event.getValue("author-url") != null) {
                        try {
                            author = new URL(event.getValue("author-url").getAsString());
                        } catch (MalformedURLException ignored) {
                        }
                    }
                    if (event.getValue("icon-url") != null) {
                        try {
                            icon = new URL(event.getValue("icon-url").getAsString());
                        } catch (MalformedURLException ignored) {
                        }
                    }
                    builder.setAuthor(event.getValue("author-text").getAsString(), (author == null ? null : author.toString()), (icon == null ? null : icon.toString()));
                    event.editMessageEmbeds(builder.build()).queue();
                }
                case "image" -> {
                    URL image = null;
                    if (event.getValue("image") != null) {
                        try {
                            image = new URL(event.getValue("image").getAsString());
                        } catch (MalformedURLException ignored) {
                        }
                    }
                    event.editMessageEmbeds(builder.setImage(image != null ? image.toString() : null).build()).queue();
                }
                case "thumbnail" -> {
                    URL image = null;
                    if (event.getValue("thumbnail") != null) {
                        try {
                            image = new URL(event.getValue("thumbnail").getAsString());
                        } catch (MalformedURLException ignored) {
                        }
                    }
                    event.editMessageEmbeds(builder.setThumbnail(image != null ? image.toString() : null).build()).queue();
                }
                case "desc" -> event.editMessageEmbeds(builder.setDescription(event.getValue("desc").getAsString()).build()).queue();

            }
        }
    }
}
