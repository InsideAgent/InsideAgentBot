package dev.jacrispys.JavaBot.commands;

import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
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

import java.sql.ResultSet;
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
        if (!event.getMessage().getContentRaw().startsWith("!complaint")) {
            if (event.getMessage().getContentRaw().contains("!clearcomplaint")) {
                complaintId.remove(event.getAuthor());
                complaintMention.remove(event.getAuthor());
                event.getMessage().reply("Complaint queue cleared!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
                event.getMessage().delete().queue();
            }
            return;
        }
        try {
            MySQLConnection connection = MySQLConnection.getInstance();
            ResultSet rs = connection.queryCommand("select * from inside_agent_bot.guilds where ID=" + event.getGuild().getId());
            rs.beforeFirst();
            if (!rs.next()) {
                event.getGuildChannel().sendMessage("Cannot execute commands before guild is indexed! Please use `!registerguild` to index your guild!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                rs.close();
                return;
            }
            rs.close();
        } catch (Exception ignored) {
            event.getGuildChannel().sendMessage("Cannot execute commands before guild is indexed! Please use `!registerguild` to index your guild!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        if (event.getMessage().getMentions().getUsers().size() != 1) return;
        User mentionedUser = event.getMessage().getMentions().getUsers().get(0);
        User sender = event.getAuthor();
        if (mentionedUser.isBot()) {
            defendAllRobots(event);
            return;
        }
        UUID  uuid = UUID.randomUUID();
        if (complaintId.get(sender) != null || complaintMention.get(sender) != null) {
            event.getMessage().reply("Cannot create a complaint when you already have an pending request! (Use !clearcomplaint to clear the request!)").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            event.getMessage().delete().queue();
            return;
        }
        complaintId.put(sender, uuid);
        complaintMention.put(sender, mentionedUser);
        Button button = Button.primary("complaint:" + uuid, "Create Complaint \uD83D\uDD2C");
        Button openTicket = Button.primary("ticket:" + uuid, "Open a Ticket \uD83D\uDCDC");
        if (event.getGuild().getBoostTier().ordinal() <= 1) {
            openTicket = openTicket.asDisabled();
        }
        Button cancelRequest = Button.danger("cancel:" + uuid, "Cancel ❌");
        event.getMessage().reply("Click below to create a complaint!").setActionRow(button, openTicket, cancelRequest).queue();
        event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            UUID buttonId = UUID.fromString(event.getComponentId().split(":")[1]);
            if (buttonId.equals(complaintId.get(event.getUser()))) {
                try {
                    switch (event.getComponentId().split(":")[0]) {
                        case ("complaint") -> {
                            TextInput reason = TextInput.create("Reason", "Reasoning for complaint:", TextInputStyle.PARAGRAPH)
                                    .setMaxLength(500)
                                    .setRequired(true)
                                    .setPlaceholder("Reason:").build();
                            Modal complaintModal = Modal.create("complaint:" + complaintId.get(event.getUser()), "User complaint (" + complaintMention.get(event.getUser()).getAsTag() + "): ").addActionRows(ActionRow.of(reason)).build();
                            event.replyModal(complaintModal).queue();
                            event.getMessage().delete().queue();

                        }
                        case ("ticket") -> {
                            //
                            User mentioned = complaintMention.get(event.getUser());
                            try {
                                MySQLConnection connection = MySQLConnection.getInstance();
                                ResultSet rs = connection.queryCommand("SELECT TicketChannel FROM inside_agent_bot.guilds WHERE ID=" + Objects.requireNonNull(event.getGuild()).getId());
                                rs.beforeFirst();
                                rs.next();
                                long channelId = rs.getLong("TicketChannel");
                                rs.close();
                                TextChannel tickets = event.getGuild().getTextChannelById(channelId);
                                if (tickets != null) {
                                    if (event.getGuild().getBoostTier().ordinal() <= 1) {
                                        event.reply("Cannot create tickets in guilds without private threads feature!").setEphemeral(true).queue();
                                        event.getMessage().delete().queue();
                                        return;
                                    }
                                    tickets.createThreadChannel(String.valueOf(buttonId), true).setInvitable(false).setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_HOUR).queue(threadChannel -> {
                                        event.reply("Ticket opened here -> " + threadChannel.getAsMention()).setEphemeral(true).queue();
                                        threadChannel.addThreadMember(event.getUser()).queue();
                                        threadChannel.addThreadMember(mentioned).queue();
                                        threadChannel.sendMessage(event.getUser().getAsMention()).queue();
                                        threadChannel.sendMessage(mentioned.getAsMention() + " we need to have a talk...").queue();
                                    });
                                } else throw new NullPointerException("Could not locate tickets channel!");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            event.getMessage().delete().queue();
                        }
                        case ("cancel") -> {
                            //
                            event.reply("Request cancelled, to open a new complaint, just use !complaint and select a option!").setEphemeral(true).queue();
                            event.getMessage().delete().queue();
                        }
                        default -> {
                            //
                            event.reply("Could not locate the action requested... Please try again later!").setEphemeral(true).queue();
                            event.getMessage().delete().queue();
                        }
                    }
                } finally {
                    if (!(event.getComponentId().split(":")[0]).equalsIgnoreCase("complaint")) {
                        complaintId.remove(event.getUser());
                        complaintMention.remove(event.getUser());
                    }
                }
            }
        } catch (Exception ignored) {
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
            privateChannel.sendMessage("> " + reason).queue();
        }));
    }
}
