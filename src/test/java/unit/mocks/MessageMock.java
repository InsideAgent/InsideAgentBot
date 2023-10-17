package unit.mocks;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.mockito.Mockito;
import unit.mocks.util.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Mock Message impls from: <a href="https://github.com/ColyTeam/JDATesting">here.</a>
 */
public class MessageMock {

    /**
     * Get a mocked {@link MessageChannel}.
     *
     * @param name              the name of the channel.
     * @param id                the id of the channel.
     * @param messageCallback   the callback used for returning the {@link Message} when for example
     *                          {@link MessageChannel#sendMessage(CharSequence)} or other methods are called.
     * @return                  a mocked {@link MessageChannel}.
     */
    public static MessageChannel getMessageChannel(String name, long id, Callback<Message> messageCallback) {
        MessageChannel channel = mock(MessageChannel.class, withSettings()
                .extraInterfaces(TextChannel.class, NewsChannel.class, MessageChannelUnion.class));
        when(channel.getName()).thenAnswer(invocation -> name);
        when(channel.getIdLong()).thenAnswer(invocation -> id);
        when(channel.getId()).thenAnswer(invocation -> String.valueOf(id));

        when(channel.sendMessage(any(CharSequence.class)))
                .thenAnswer(invocation -> getMessageCreateAction(messageCallback,
                        getMessage(invocation.getArgument(0), channel)));

        when(channel.sendMessage(any(MessageCreateData.class)))
                .thenAnswer(invocation -> getMessageCreateAction(messageCallback,
                        invocation.getArgument(0)));

        when(channel.sendMessageEmbeds(any(MessageEmbed.class), any(MessageEmbed[].class)))
                .thenAnswer(invocation -> {
                    List<MessageEmbed> embeds = invocation.getArguments().length == 1 ? new ArrayList<>() :
                            Arrays.asList(invocation.getArgument(1));
                    embeds.add(invocation.getArgument(0));
                    return getMessageCreateAction(messageCallback, getMessage(null, embeds, channel));
                });

        when(channel.sendMessageEmbeds(anyList()))
                .thenAnswer(invocation -> getMessageCreateAction(messageCallback,
                        getMessage(null, invocation.getArgument(0), channel)));

        return channel;
    }

    /**
     * Get a mocked {@link MessageCreateAction}.
     *
     * @param messageCallback   the message callback that well return the {@link Message} when
     *                          {@link MessageCreateAction#queue()} is executed.
     * @param message           the message that will be used by the {@link Callback}.
     * @return                  a mocked {@link MessageCreateAction}.
     */
    public static MessageCreateAction getMessageCreateAction(Callback<Message> messageCallback, Message message) {
        MessageCreateAction messageAction = mock(MessageCreateAction.class);
        Mockito.doAnswer(invocation -> {
            messageCallback.callback(message);
            return null;
        }).when(messageAction).queue();
        return messageAction;
    }

    /**
     * Get a mocked {@link Message}.
     *
     * @param content   the content of the message. This is the raw, displayed and stripped content.
     * @param channel   the {@link MessageChannel} the message would be sent in.
     * @return          a mocked {@link Message}.
     */
    public static Message getMessage(String content, MessageChannel channel) {
        return getMessage(content, new ArrayList<>(), channel);
    }

    /**
     * Get a mocked {@link Message}.
     *
     * @param content   the content of the message. This is the raw, displayed and stripped content.
     * @param embeds    a list of {@link MessageEmbed}s that this message contains.
     * @param channel   the {@link MessageChannel} the message would be sent in.
     * @return          a mocked {@link Message}.
     */
    public static Message getMessage(String content, List<MessageEmbed> embeds, MessageChannel channel) {
        Message message = mock(Message.class);
        when(message.getContentRaw()).thenAnswer(invocation -> content);
        when(message.getContentDisplay()).thenAnswer(invocation -> content);
        when(message.getContentStripped()).thenAnswer(invocation -> content);
        when(message.getChannel()).thenAnswer(invocation -> channel);
        when(message.getEmbeds()).thenAnswer(invocation -> embeds);
        return message;
    }
}
