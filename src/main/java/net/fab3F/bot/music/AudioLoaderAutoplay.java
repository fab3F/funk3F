package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AudioLoaderAutoplay extends AbstractAudioLoadResultHandler {
    private final Logger logger = LoggerFactory.getLogger(AudioLoaderAutoplay.class);
    private final TrackScheduler scheduler;
    private final String input;
    private final long channelId;

    public AudioLoaderAutoplay(String input, TrackScheduler scheduler, long channelId) {
        this.scheduler = scheduler;
        this.input = input;
        this.channelId = channelId;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        track.setUserData(new CustomTrackData(0, this.channelId, true));
        this.scheduler.enqueue(track, false);
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        CustomTrackData userData = new CustomTrackData(0, this.channelId, true);
        for(Track t : result.getTracks()){
            t.setUserData(userData);
        }
        this.scheduler.enqueuePlaylist(result.getTracks());
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        final List<Track> tracks = result.getTracks();
        if (tracks.isEmpty()) {
            this.handleAutoplayError("Autoplay Anfrage fehlgeschlagen! Die Suche hat leider keine Ergebnisse geliefert.", "40: Autoplay had no SearchResults");
        } else {
            final Track track = tracks.get(0);
            track.setUserData(new CustomTrackData(0, this.channelId, true));
            this.scheduler.enqueue(track, false);
        }
    }

    @Override
    public void noMatches() {
        this.handleAutoplayError("Autoplay Anfrage fehlgeschlagen! Es gab keine Übereinstimmungen.", "41: Autoplay had no Matches");
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        this.handleAutoplayError("Autoplay Anfrage fehlgeschlagen! Die Medien konnten nicht geladen werden.", "42: Autoplay Load Failed\nError Message: " + result.getException().getMessage());
    }

    private void handleAutoplayError(String dcMsg, String errMsg){
        this.scheduler.stopAutoplay();
        TextChannel channel = this.scheduler.musicHandler.resolveTextChannel(this.scheduler.guildId, this.channelId);
        if(channel != null){
            channel.sendMessage(dcMsg + "\nAutoplay wurde deaktiviert.").queue();
        }
        logger.error(errMsg + "\nThis was the input: " + input);
    }

}