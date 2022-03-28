package com.jacrispys.JavaBot.Commands;

import net.dv8tion.jda.api.entities.ChannelType;
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
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
;import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ComplaintCommand extends ListenerAdapter  {

    private static final Map<User, UUID> complaintId = new HashMap<>();
    private static final Map<User, User> complaintMention = new HashMap<>();

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(event.isFromType(ChannelType.PRIVATE)) return;
        if(!event.getMessage().getContentRaw().contains("!complaint")) {
            if(event.getMessage().getContentRaw().contains("!clearcomplaint")) {
                complaintId.remove(event.getAuthor());
                complaintMention.remove(event.getAuthor());
                event.getMessage().reply("Complaint queue cleared!").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
                event.getMessage().delete().queue();
            }
            return;
        }
        if(event.getMessage().getMentionedUsers().size() != 1) return;
        User mentionedUser = event.getMessage().getMentionedUsers().get(0);
        User sender = event.getAuthor();
        if(mentionedUser.isBot()) {defendAllRobots(event); return;}
        UUID uuid = UUID.randomUUID();
        Button button = Button.danger("complaint:" + uuid,"Create Complaint?");
        if(complaintId.get(sender) != null || complaintMention.get(sender) != null) {
            event.getMessage().reply("Cannot create a complaint when you already have an pending request! (Use !clearcomplaint to clear the request!)").queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
            event.getMessage().delete().queue();
            return;
        }
        complaintId.put(sender, uuid);
        complaintMention.put(sender, mentionedUser);

        event.getMessage().reply("Click below to create a complaint!").setActionRow(button).queue();
        event.getMessage().delete().queue();

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(event.getComponentId().equals("complaint:" + complaintId.get(event.getUser()))) {
            TextInput reason = TextInput.create("Reason","Reasoning for complaint:", TextInputStyle.PARAGRAPH)
                    .setMaxLength(500)
                    .setRequired(true)
                    .setPlaceholder("Reason:").build();


            Modal complaintModal = Modal.create("complaint:" + complaintId.get(event.getUser()), "User complaint (" +  complaintMention.get(event.getUser()).getAsTag() + "): ").addActionRows(ActionRow.of(reason)).build();
            event.replyModal(complaintModal).queue();
            event.getMessage().delete().queue();
        } else {
            event.reply("Only the user who issued the complaint can use this feature!").setEphemeral(true).queue();
        }

    }
    public void onModalInteraction(@Nonnull ModalInteractionEvent event)
    {
        if (event.getModalId().equals("complaint:" + complaintId.get(event.getUser())))
        {
            String reason = event.getValue("Reason").getAsString();
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
        complaint.openPrivateChannel().queue((privateChannel ->  {
            privateChannel.sendMessage("User: " + complainer.getAsTag() + ", has complained about you with the following message: ").queue();
            privateChannel.sendMessage("> `" + reason + "`").queue();
        }));
    }
}
