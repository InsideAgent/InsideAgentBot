package dev.jacrispys.JavaBot.music;

import com.neovisionaries.i18n.CountryCode;
import dev.jacrispys.JavaBot.music.objects.Genres;
import dev.jacrispys.JavaBot.utils.mysql.MySQLConnection;
import dev.jacrispys.JavaBot.utils.SpotifyManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static dev.jacrispys.JavaBot.music.GuildAudioManager.nowPlayingId;

public class GenerateGenrePlaylist extends ListenerAdapter {

    public GenerateGenrePlaylist() {

    }

    public static Map<User, Long> reactMessage = new HashMap<>();
    public static Map<User, Integer> limit = new HashMap<>();

    public Recommendations generatePlaylistFromGenre(String genres, int limit) throws IOException, ParseException, SpotifyWebApiException {
        final GetRecommendationsRequest request = SpotifyManager.getInstance().getSpotifyApi().getRecommendations().market(CountryCode.US).seed_genres(genres).limit(limit).build();
        return request.execute();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            Guild fromButtonGuild = event.getGuild();
            GuildAudioManager audioManager =  GuildAudioManager.getGuildAudioManager(fromButtonGuild);
            String buttonName = event.getComponentId().split(":")[0];
            int pages = (int) Math.ceil((float) Genres.getValues().size() / 10);
            if(event.isAcknowledged()) return;
            if (event.getUser().getIdLong() != Long.parseLong(event.getComponentId().split(":")[1])) {
                event.reply("Only the owner of this embed can edit it!").setEphemeral(true).queue();
                return;
            }

            switch (buttonName) {
                case ("firstGenre") -> {
                    if (genrePage != 1) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), 1, event.getUser()).build()).queue();
                    } else {
                        if (!event.isAcknowledged()) {
                            event.reply("You are already on the first page!").setEphemeral(true).queue();
                        }
                    }
                }
                case ("backGenre") -> {
                    if (genrePage <= 1) {
                        if (genrePage == 0) {
                            if (!event.isAcknowledged()) {
                                event.reply("What? Did you expect page 0 or... HEY WAIT A MINUTE \uD83D\uDE21").setEphemeral(true).queue();
                            }
                        } else {
                            if (!event.isAcknowledged()) {
                                event.reply("What? Did you expect page 0 or something?").setEphemeral(true).queue();
                            }
                        }
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), genrePage - 1, event.getUser()).build()).queue();
                }
                case ("submitGenres") -> {
                    event.getMessage().delete().queue();
                    event.reply("Generating playlist please wait...").setEphemeral(true).queue();
                    VoiceChannel channel;
                    assert event.getMember() != null;
                    assert event.getMember().getVoiceState() != null;
                    assert event.getMember().getVoiceState().getChannel() != null;
                    channel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
                    if(channel == null) {
                        event.getHook().editOriginal("Cannot add tracks, as you are not in a voice channel!").queue();
                        chosenGenres.remove(event.getUser());
                        reactMessage.remove(event.getUser());
                        positionList.clear();
                        return;
                    }
                    updateMusicChannel(event.getGuild(), event.getGuildChannel().asTextChannel());
                    GenerateGenrePlaylist genrePlaylist = new GenerateGenrePlaylist();
                    StringBuilder genre = new StringBuilder();
                    chosenGenres.get(event.getUser()).forEach(pos -> genre.append(Genres.getValues().get(pos)).append(","));
                    Recommendations requestData = genrePlaylist.generatePlaylistFromGenre(genre.toString(), limit.get(event.getUser()));
                    event.getHook().editOriginal(GuildAudioManager.getGuildAudioManager(event.getGuild()).generateRadio(requestData, channel, event.getUser())).queue();

                }
                case ("nextGenre") -> {
                    if (genrePage >= pages) {
                        if (!event.isAcknowledged()) {
                            event.reply("Cannot go further than the final page!").setEphemeral(true).queue();
                        }
                        return;
                    }
                    event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), genrePage + 1, event.getUser()).build()).queue();
                }
                case ("lastGenre") -> {
                    if (genrePage != pages) {
                        event.editMessageEmbeds(updateEmbed(event.getMessage().getEmbeds().get(0), pages, event.getUser()).build()).queue();
                    } else {
                        if (!event.isAcknowledged()) {
                            event.reply("You are already on the final page!").setEphemeral(true).queue();
                        }
                    }
                }
                case ("togglePlayer") -> {
                    audioManager.togglePlayer();
                    event.editMessage(event.getMessage()).queue();
                }
                case ("skipTrack") -> {
                    event.deferReply().queue();
                    nowPlayingId.put(fromButtonGuild, event.getMessage().getIdLong());
                    if(audioManager.audioPlayer.getPlayingTrack() == null) {
                        event.getHook().editOriginal(event.getMessage()).queue();
                    } else {
                        event.getHook().editOriginal(audioManager.skipTrack()).queue();
                    }
                }
                case ("showQueue") -> {
                    event.reply(audioManager.displayQueue()).setEphemeral(true).queue();
                    if (!event.isAcknowledged()) {
                        event.editMessage(event.getMessage()).queue();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int genrePage;

    private EmbedBuilder updateEmbed(MessageEmbed embed, int page, User user) {
        genrePage = page;
        EmbedBuilder eb = new EmbedBuilder(embed);
        eb.clearFields();
        StringBuilder genres = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            try {
                String genre = Genres.getValues().get((page -1) * 10 + i);
                if(chosenGenres.containsKey(user) && chosenGenres.get(user).contains((page -1) * 10 + (i))) {
                    genres.append("`").append("✅ ").append(genre).append("` \n");
                    continue;
                }
                genres.append("`").append(i + 1).append(". ").append(genre).append("` \n");
            } catch (IndexOutOfBoundsException ex) {
                break;
            }
        }

        String pageNumber = "Page " + genrePage + "/" + (int) Math.ceil((float) Genres.getValues().size() / 10);
        int size = chosenGenres.containsKey(user) ? chosenGenres.get(user).size() : 0;
        eb.setFooter(pageNumber + " | Max 5 genres! | "+ size + "/5 Currently Selected!");

        eb.addField("React to genres you want added!", genres.toString(), false);
        return eb;


    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(reactMessage.containsValue(event.getMessageIdLong()) && reactMessage.getOrDefault(event.getUser(), 0L) != event.getMessageIdLong()) {
            if(event.getUser().equals(event.getGuild().getSelfMember().getUser())) return;
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }
        if(reactMessage.getOrDefault(event.getUser(), 0L) == event.getMessageIdLong()) {
            event.retrieveMessage().queue(msg -> {
                List<UnicodeEmoji> unicodes = new ArrayList<>();
                msg.getReactions().forEach(reaction -> unicodes.add(reaction.getEmoji().asUnicode()));
                msg.editMessageEmbeds(addGenre(msg.getEmbeds().get(0), unicodes.indexOf(event.getReaction().getEmoji().asUnicode()), event.getUser())).queue();
                Button button =  msg.getButtonById("submitGenres:" + event.getUserIdLong());
                if(positionList.size() > 0 && !(positionList.size() > 5)) {
                    button = button.asEnabled();
                } else {
                    button = button.asDisabled();
                }
                List<Button> buttons = msg.getButtons();
                buttons.set(2, button);
                msg.editMessageComponents(ActionRow.of(buttons)).queue();
                event.getReaction().removeReaction(event.getUser()).queue();
            });
        }
    }

    public static Map<User, List<Integer>> chosenGenres = new HashMap<>();
    private static final List<Integer> positionList = new ArrayList<>();

    protected MessageEmbed addGenre(MessageEmbed embed, int position, User user) {
        EmbedBuilder eb = new EmbedBuilder(embed);
        List<String> embedLines = new ArrayList<>(List.of(embed.getFields().get(0).getValue().split("\n")));
        int page = genrePage;
        if((page -1) * 10 + (position) < 125) {
            String line = embedLines.get(position);
            if (!line.startsWith("`✅")) {
                line = line.replace((position + 1) + ".", "✅");
                embedLines.set(position, line);
                positionList.add((page - 1) * 10 + (position));
            } else {
                line = line.replace("✅", (position + 1) + ".");
                embedLines.set(position, line);
                positionList.removeIf(val -> val.equals((page - 1) * 10 + (position)));
            }
            chosenGenres.put(user, positionList);
        }
        embedLines = embedLines.stream()
                .map(s -> s+"\n")
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        embedLines.forEach(builder::append);
        String title = embed.getFields().get(0).getName();
        eb.clearFields();
        assert title != null;
        eb.addField(title, builder.toString(), false);

        eb.setFooter("Page " + page + "/13" + " | Max 5 genres! | "+ chosenGenres.get(user).size() + "/5 Currently Selected!");

        return eb.build();
    }

    protected void updateMusicChannel(Guild guild, TextChannel channel) {
        try {
            MySQLConnection.getInstance().setMusicChannel(Objects.requireNonNull(guild), channel.getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if(reactMessage.containsValue(event.getMessageIdLong())) {
            reactMessage.keySet().forEach(key -> reactMessage.values().forEach(value -> {
                if(value.equals(event.getMessageIdLong())) {
                    if(reactMessage.get(key).equals(value)) {
                        chosenGenres.remove(key);
                    }
                }
            }));
            reactMessage.values().removeIf(val -> val.equals(event.getMessageIdLong()));
        }
    }
}
