package net.fab3F.bot.listener;

import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.player.Track;
import net.fab3F.bot.music.MusicHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LavaListener {
    private final Logger logger = LoggerFactory.getLogger(LavaListener.class);
    private final MusicHandler mH;
    public LavaListener(MusicHandler mH){
        this.mH = mH;
    }

    public void handleTrackStart(TrackStartEvent event){
        Track track = event.getTrack();
        logger.debug("[Lava-Listener] Node: '{}'; Guild: '{} ({})' ; Track started -> {}", event.getNode().getName(), mH.getShardManager().getGuildById(event.getGuildId()).getName(), event.getGuildId(), track.getInfo().getTitle());
        mH.getOrCreateTrackScheduler(event.getGuildId()).onTrackStart(track);
    }

    public void handleTrackEnd(TrackEndEvent event){
        logger.debug("[Lava-Listener] Node '{}': Track ended -> {} (reason: {})", event.getNode().getName(), event.getTrack().getInfo().getTitle(), event.getEndReason());
        mH.getOrCreateTrackScheduler(event.getGuildId()).onTrackEnd(event.getTrack(), event.getEndReason());
    }

    public void handleTrackException(TrackExceptionEvent event){
        logger.debug("[Lava-Listener] Node '{}': Track exception -> {} (error: {})", event.getNode().getName(), event.getTrack().getInfo().getTitle(), event.getException().getMessage());

        // TODO: Play next song or stop
    }

    public void handleTrackStuck(TrackStuckEvent event){
        logger.debug("[Lava-Listener] Node '{}': Track stuck -> {} (thresholdMs: {})", event.getNode().getName(), event.getTrack().getInfo().getTitle(), event.getThresholdMs());

        // TODO: Play next song or stop
    }

    public void handleWebsocketClosed(WebSocketClosedEvent event){
        logger.debug("[Lava-Listener] Node '{}': WebSocket closed (code: {}, reason: {}, byRemote: {})", event.getNode().getName(), event.getCode(), event.getReason(), event.getByRemote());

        // TODO: stop
    }


}
