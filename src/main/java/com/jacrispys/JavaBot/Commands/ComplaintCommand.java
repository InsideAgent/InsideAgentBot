package com.jacrispys.JavaBot.Commands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ComplaintCommand extends ListenerAdapter {

    private static final Map<User, UUID> complaintId = new HashMap<>();
    private static final Map<User, User> complaintMention = new HashMap<>();

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;
        if (!event.getMessage().getContentRaw().contains("!complaint")) {
            if (event.getMessage().getContentRaw().contains("!clearcomplaint")) {
                complaintId.remove(event.getAuthor());
                complaintMention.remove(event.getAuthor());
                event.getMessage().reply("Complaint queue cleared!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
                event.getMessage().delete().queue();
            }
            return;
        }
        if (event.getMessage().getMentionedUsers().size() != 1) return;
        User mentionedUser = event.getMessage().getMentionedUsers().get(0);
        User sender = event.getAuthor();
        if (mentionedUser.isBot()) {
            defendAllRobots(event);
            return;
        }
        UUID uuid = UUID.randomUUID();
        if (complaintId.get(sender) != null || complaintMention.get(sender) != null) {
            event.getMessage().reply("Cannot create a complaint when you already have an pending request! (Use !clearcomplaint to clear the request!)").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            event.getMessage().delete().queue();
            return;
        }
        complaintId.put(sender, uuid);
        complaintMention.put(sender, mentionedUser);
        Button button = Button.primary("complaint:" + uuid, "Create Complaint?");
        Button openTicket = Button.primary("ticket:" + uuid, "Open a Ticket?");
        Button cancelRequest = Button.danger("cancel:" + uuid, "Cancel?");
        event.getMessage().reply("Click below to create a complaint!").setActionRow(button, openTicket, cancelRequest).queue();
        event.getMessage().delete().queue();

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        UUID buttonId = UUID.fromString(event.getComponentId().split(":")[1]);
        if (buttonId.equals(complaintId.get(event.getUser()))) {
            try {
                switch (event.getComponentId().split(":")[0]) {
                    case ("complaint"):
                        TextInput reason = TextInput.create("Reason", "Reasoning for complaint:", TextInputStyle.PARAGRAPH)
                                .setMaxLength(500)
                                .setRequired(true)
                                .setPlaceholder("Reason:").build();


                        Modal complaintModal = Modal.create("complaint:" + complaintId.get(event.getUser()), "User complaint (" + complaintMention.get(event.getUser()).getAsTag() + "): ").addActionRows(ActionRow.of(reason)).build();
                        event.replyModal(complaintModal).queue();
                        event.getMessage().delete().queue();
                        return;
                    case ("ticket"):
                        //
                        Yaml yaml = new Yaml();
                        InputStream is;
                        try {
                            is = getClass().getClassLoader().getResourceAsStream("guildData.yml");
                            Map<String, Map<String, String>> values = yaml.load(is);
                            if (values.containsKey(Long.parseLong(event.getGuild().getId()))) {
                                Map<String, String> guildData = values.get(Long.parseLong(event.getGuild().getId()));
                                String ticketChannel = guildData.get("tickets");
                                TextChannel tickets = event.getGuild().getTextChannelById(ticketChannel);
                                tickets.createThreadChannel(String.valueOf(buttonId), false).setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_HOUR).queue(threadChannel -> event.reply("Ticket opened here -> " + threadChannel.getAsMention()).setEphemeral(true).queue());

                            }


                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }

                        event.getMessage().delete().queue();
                        return;
                    case ("cancel"):
                        //
                        event.reply("Request cancelled, to open a new complaint, just use !complaint and select a option!").setEphemeral(true).queue();
                        event.getMessage().delete().queue();
                        return;
                    default:
                        //
                        event.reply("Could not locate the action requested... Please try again later!").setEphemeral(true).queue();
                        event.getMessage().delete().queue();
                }
            } finally {
                if (!(event.getComponentId().split(":")[0]).equalsIgnoreCase("complaint")) {
                    complaintId.remove(event.getUser());
                    complaintMention.remove(event.getUser());
                }
            }
        } else {
            event.reply("Only the user who issued the complaint can use this feature!").setEphemeral(true).queue();
        }

    }

    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("complaint:" + complaintId.get(event.getUser()))) {
            String reason = Objects.requireNonNull(event.getValue("Reason")).getAsString();
            User mentionedUser = complaintMention.get(event.getUser());

            collectComplaint(reason, event.getUser(), mentionedUser);
            event.reply("Thanks you for helping keep this server friendly! \uD83E\uDD1D Your complaint has been recorded & sent to server moderators. ☑️").setEphemeral(true).queue();
            complaintId.remove(event.getUser());
            complaintMention.remove(event.getUser());
        }
    }

    protected void defendAllRobots(MessageReceivedEvent event) {
        event.getMessage().reply("Test...").mentionRepliedUser(false).queue();
    }

    protected void collectComplaint(String reason, User complainer, User complaint) {
        complaint.openPrivateChannel().queue((privateChannel -> {
            privateChannel.sendMessage("User: " + complainer.getAsTag() + ", has complained about you with the following message: ").queue();
            privateChannel.sendMessage("> `" + reason + "`").queue();
        }));
    }
}
