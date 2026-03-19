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
    private final boolean playAsFirst;
    public AudioLoader(String input, SlashCommandInteractionEvent event, TrackScheduler scheduler, boolean playAsFirst) {
        this.event = event;
        this.scheduler = scheduler;
        this.input = input;
        this.playAsFirst = playAsFirst;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        track.setUserData(new CustomTrackData(event.getUser().getIdLong(), event.getChannel().asTextChannel().getIdLong(), false));

        this.scheduler.enqueue(track, this.playAsFirst);

        event.getHook().sendMessage("Song zur Wiedergabeliste hinzugefügt: **`" + track.getInfo().getTitle() + "`**").queue();
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
        CustomTrackData userData = new CustomTrackData(event.getUser().getIdLong(), event.getChannel().asTextChannel().getIdLong(), false);
        for(Track t : result.getTracks()){
            t.setUserData(userData);
        }
        this.scheduler.enqueuePlaylist(result.getTracks());

        event.getHook()
                .sendMessage("Es wurden **`" + trackCount + "`** Songs von **`" + result.getInfo().getName() + "`** zu Wiedergabeliste hinzugefügt!")
                .queue();
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        final List<Track> tracks = result.getTracks();
        String msg;
        if (tracks.isEmpty()) {
            msg = "Keine Ergebnisse gefunden für folgende Eingabe: " + input;
        } else {
            final Track track = tracks.get(0);
            track.setUserData(new CustomTrackData(event.getUser().getIdLong(), event.getChannel().asTextChannel().getIdLong(), false));
            this.scheduler.enqueue(track, this.playAsFirst);
            msg = "Song zur Wiedergabeliste hinzugefügt: **`" + track.getInfo().getTitle() + "`**";
        }
        event.getHook().sendMessage(msg).queue();
    }

    @Override
    public void noMatches() {
        event.getHook().sendMessage("Keine Ergebnisse gefunden für folgende Eingabe: " + input).queue();
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        event.getHook().sendMessage("Beim Laden eines Songs ist ein Fehler aufgetreten. Falls eine Playlist abgespielt werden soll, stelle sicher, dass sie nicht auf privat gestellt ist. Eingabe:\n" + input).queue();
    }
}