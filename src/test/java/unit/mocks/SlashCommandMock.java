package unit.mocks;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.mockito.Mockito;
import unit.mocks.util.Callback;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static unit.mocks.MessageMock.getMessage;


/**
 * Mock SlashCommandInteraction event from: <a href="https://github.com/ColyTeam/JDATesting">here.</a>
 */
public class SlashCommandMock {

    /**
     * Get a mocked {@link SlashCommandInteractionEvent}.
     *
     * @param channel         the channel this event would be executed.
     * @param name            the name of the slash command.
     * @param subcommandName  the name of the subcommand of the slash command.
     * @param subcommandGroup the subcommand group of the slash command.
     * @param options         a map with all options for the slash command a user would have inputted.
     * @param messageCallback a callback to receive messages that would be sent back to the channel with.
     * @param deferReply      a callback that is called when a deferred reply is called. The boolean is true when
     *                        the message is ephemeral and false if not.
     *                        {@link MessageChannel#sendMessage(CharSequence)}
     * @return a mocked {@link SlashCommandInteractionEvent}.
     */
    public static SlashCommandInteractionEvent getSlashCommandInteractionEvent(MessageChannel channel, String name,
                                                                               String subcommandName,
                                                                               String subcommandGroup,
                                                                               Map<String, Object> options,
                                                                               Member member,
                                                                               Callback<Message> messageCallback,
                                                                               Callback<Boolean> deferReply) {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        when(event.getName()).thenAnswer(invocation -> name);
        when(event.getSubcommandName()).thenAnswer(invocation -> subcommandName);
        when(event.getSubcommandGroup()).thenAnswer(invocation -> subcommandGroup);
        when(event.getChannel()).thenAnswer(invocation -> channel);
        when(event.getMember()).thenAnswer(invocationOnMock -> member);

        when(event.getOption(anyString())).thenAnswer(invocation -> {
            OptionMapping mapping = mock(OptionMapping.class);
            // why
            when(mapping.getAsAttachment()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsString()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsBoolean()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsLong()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsInt()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsDouble()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsMentionable()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsMember()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsUser()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsRole()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));
            when(mapping.getAsChannel()).thenAnswer(inv -> options.get((String) invocation.getArgument(0)));

            return mapping;
        });

        when(event.reply(anyString())).thenAnswer(invocation ->
                getReplyCallbackAction(getMessage(invocation.getArgument(0), channel), messageCallback));
        when(event.reply(any(MessageCreateData.class))).thenAnswer(invocation ->
                getReplyCallbackAction(getMessage(invocation.getArgument(0, MessageCreateData.class).getContent(),
                        channel), messageCallback));
        when(event.replyEmbeds(anyList())).thenAnswer(invocation ->
                getReplyCallbackAction(getMessage(null, invocation.getArgument(0), channel),
                        messageCallback));
        when(event.replyEmbeds(any(MessageEmbed.class), any(MessageEmbed[].class))).thenAnswer(invocation -> {
            List<MessageEmbed> embeds = invocation.getArguments().length == 1 ? new ArrayList<>() :
                    Arrays.asList(invocation.getArgument(1));
            embeds.add(invocation.getArgument(0));
            return getReplyCallbackAction(getMessage(null, embeds, channel), messageCallback);
        });

        when(event.deferReply()).thenAnswer(invocation -> {
            deferReply.callback(false);
            return mock(ReplyCallbackAction.class);
        });

        when(event.deferReply(any(Boolean.class))).thenAnswer(invocation -> {
            deferReply.callback(invocation.getArgument(0));
            return mock(ReplyCallbackAction.class);
        });

        return event;
    }

    /**
     * Get a mocked {@link SlashCommandInteractionEvent}.
     *
     * @param channel         the channel this event would be executed.
     * @param name            the name of the slash command.
     * @param subcommandName  the name of the subcommand of the slash command.
     * @param subcommandGroup the subcommand group of the slash command.
     * @param options         a map with all options for the slash command a user would have inputted.
     * @param messageCallback a callback to receive messages that would be sent back to the channel with.
     *                        {@link MessageChannel#sendMessage(CharSequence)}
     * @return a mocked {@link SlashCommandInteractionEvent}.
     */
    public static SlashCommandInteractionEvent getSlashCommandInteractionEvent(MessageChannel channel, String name,
                                                                               String subcommandName,
                                                                               String subcommandGroup,
                                                                               Map<String, Object> options,
                                                                               Member member,
                                                                               Callback<Message> messageCallback) {
        return getSlashCommandInteractionEvent(channel, name, subcommandName, subcommandGroup, options, member, messageCallback,
                new Callback<>());
    }

    public static SlashCommandInteractionEvent getSlashCommandInteractionEvent(String cmdName,
                                                                               Member member
                                                                               ) {
        return getSlashCommandInteractionEvent(null, cmdName, null, null, null, member, null,
                new Callback<>());
    }

    /**
     * Get a mocked {@link ReplyCallbackAction}.
     *
     * @param message           the message that this reply should produce.
     * @param messageCallback   the callback for receiving the message.
     * @return                  a mocked {@link ReplyCallbackAction}.
     */
    private static ReplyCallbackAction getReplyCallbackAction(Message message, Callback<Message> messageCallback) {
        ReplyCallbackAction action = mock(ReplyCallbackAction.class);

        Mockito.doAnswer(invocation -> {
            messageCallback.callback(message);
            return null;
        }).when(action).queue();

        when(action.setEphemeral(anyBoolean())).thenAnswer(invocation -> {
            when(message.isEphemeral()).thenReturn(true);
            return action;
        });
        return action;
    }


    public static Message testSlashCommandReply(EventListener listener, String name, Member member) throws InterruptedException {
        Callback<Message> messageCallback = new Callback<>();

        MessageChannel channel = MessageMock.getMessageChannel("test-channel", 0L, messageCallback);
        SlashCommandInteractionEvent event = getSlashCommandInteractionEvent(channel, name, null, null,
                null, member, messageCallback);

        listener.onEvent(event);
        return messageCallback.await();
    }



}
