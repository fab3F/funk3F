package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private final SlashCommandInteractionEvent event;
    private final TrackScheduler scheduler;
    private final String input;

    public AudioLoader(String input, SlashCommandInteractionEvent event, TrackScheduler scheduler) {
        this.event = event;
        this.scheduler = scheduler;
        this.input = input;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        track.setUserData(new CustomTrackData(this.input, event.getUser().getName(), event.getChannel().asTextChannel()));

        this.scheduler.enqueue(track);

        final var trackTitle = track.getInfo().getTitle();

        event.getHook().sendMessage("Added to queue: " + trackTitle).queue();
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
        event.getHook()
                .sendMessage("Added " + trackCount + " tracks to the queue from " + result.getInfo().getName() + "!")
                .queue();

        this.scheduler.enqueuePlaylist(result.getTracks());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            event.getHook().sendMessage("No tracks found!").queue();
            return;
        }

        final Track firstTrack = tracks.get(0);

        event.getHook().sendMessage("Adding to queue: " + firstTrack.getInfo().getTitle()).queue();

        this.scheduler.enqueue(firstTrack);
    }

    @Override
    public void noMatches() {
        event.getHook().sendMessage("No matches found for your input!").queue();
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        event.getHook().sendMessage("Failed to load track! " + result.getException().getMessage()).queue();
    }
}