package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private final Logger logger = LoggerFactory.getLogger(AudioLoader.class);
    private final SlashCommandInteractionEvent event;
    private final TrackScheduler scheduler;
    private final String input;
    private final boolean playAsFirst;
    private final boolean isAutoplay;
    private final long channelId;
    private final long userId;

    // Use for normal
    public AudioLoader(String input, SlashCommandInteractionEvent event, TrackScheduler scheduler, boolean playAsFirst) {
        this.isAutoplay = false;
        this.channelId = event.getChannelIdLong();
        this.userId = event.getUser().getIdLong();

        this.event = event;
        this.scheduler = scheduler;
        this.input = input;
        this.playAsFirst = playAsFirst;
    }
    // Use for AutoPlay
    public AudioLoader(String input, TrackScheduler scheduler, long channelId) {
        this.isAutoplay = true;
        this.event = null;
        this.playAsFirst = false;
        this.userId = 0;

        this.scheduler = scheduler;
        this.input = input;
        this.channelId = channelId;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        track.setUserData(new CustomTrackData(this.userId, this.channelId, this.isAutoplay));
        this.scheduler.enqueue(track, this.playAsFirst);

        if(!this.isAutoplay){
            event.getHook().sendMessage("Song zur Wiedergabeliste hinzugefügt: **`" + track.getInfo().getTitle() + "`**").queue();
        }

    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        CustomTrackData userData = new CustomTrackData(this.userId, this.channelId, this.isAutoplay);
        for(Track t : result.getTracks()){
            t.setUserData(userData);
        }
        this.scheduler.enqueuePlaylist(result.getTracks());

        if(!this.isAutoplay){
            final int trackCount = result.getTracks().size();
            event.getHook()
                    .sendMessage("Es wurden **`" + trackCount + "`** Songs von **`" + result.getInfo().getName() + "`** zu Wiedergabeliste hinzugefügt!")
                    .queue();
        }
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        final List<Track> tracks = result.getTracks();
        String msg = "";
        if (tracks.isEmpty()) {
            if(this.isAutoplay){
                this.handleAutoplayError("Autoplay Anfrage fehlgeschlagen! Die Suche hat leider keine Ergebnisse geliefert.", "40: Autoplay had no SearchResults");
            }else{
                msg = "Keine Ergebnisse gefunden für folgende Eingabe: " + input;
            }
        } else {
            final Track track = tracks.get(0);
            track.setUserData(new CustomTrackData(this.userId, this.channelId, this.isAutoplay));
            this.scheduler.enqueue(track, this.playAsFirst);
            if(!this.isAutoplay){
                msg = "Song zur Wiedergabeliste hinzugefügt: **`" + track.getInfo().getTitle() + "`**";
            }
        }
        if(!this.isAutoplay){
            event.getHook().sendMessage(msg).queue();
        }
    }

    @Override
    public void noMatches() {
        if(this.isAutoplay){
            this.handleAutoplayError("Autoplay Anfrage fehlgeschlagen! Es gab keine Übereinstimmungen.", "41: Autoplay had no Matches");
        }else{
            event.getHook().sendMessage("Keine Ergebnisse gefunden für folgende Eingabe: " + input).queue();
        }
    }


    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        if(this.isAutoplay){
            this.handleAutoplayError("Autoplay Anfrage fehlgeschlagen! Die Medien konnten nicht geladen werden.", "42: Autoplay Load Failed\nError Message: " + result.getException().getMessage());
        }else{
            event.getHook().sendMessage("Beim Laden eines Songs ist ein Fehler aufgetreten. Falls eine Playlist abgespielt werden soll, stelle sicher, dass sie nicht auf privat gestellt ist. Eingabe:\n" + input).queue();
        }
    }

    private void handleAutoplayError(String dcMsg, String errMsg){
        logger.error("{}\nThis was the input: {}", errMsg, input);
        if (input.startsWith("ytsearch:")) {
            return;
        }
        this.scheduler.stopAutoplay();
        TextChannel channel = this.scheduler.getGuild().getChannelById(TextChannel.class, channelId);
        if(channel != null){
            channel.sendMessage(dcMsg + "\nAutoplay wurde deaktiviert.").queue();
        }
    }

}