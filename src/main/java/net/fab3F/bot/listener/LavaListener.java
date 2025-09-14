package net.fab3F.bot.listener;

import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.fab3F.Main;
import net.fab3F.bot.music.CustomTrackData;
import net.fab3F.bot.music.MusicHelper;
import net.fab3F.customTools.Logger;

public class LavaListener {
    Logger logger;
    public LavaListener(Logger logger){
        this.logger = logger;
    }

    public void handleTrackStart(TrackStartEvent event){
        TrackInfo info = event.getTrack().getInfo();
        logger.debug(String.format(
                "[Lava-Listener] Node: '%s'; Guild: '%s (%s)' ; Track started -> %s",
                event.getNode().getName(),
                Main.bot.getShardManager().getGuildById(event.getGuildId()).getName(),
                event.getGuildId(),
                info.getTitle()
        ));
        String msg = "Jetzt spielt: **`" + info.getTitle() + "`** von **`" + info.getAuthor() + "`** [" + MusicHelper.calcDuration((int)info.getLength()) + "]";
        event.getTrack().getUserData(CustomTrackData.class).channel().sendMessage(msg).queue();
    }

    public void handleTrackEnd(TrackEndEvent event){
        logger.debug(String.format(
                "[Lava-Listener] Node '%s': Track ended -> %s (reason: %s)",
                event.getNode().getName(),
                event.getTrack().getInfo().getTitle(),
                event.getEndReason()
        ));
    }

    public void handleTrackException(TrackExceptionEvent event){
        logger.debug(String.format(
                "[Lava-Listener] Node '%s': Track exception -> %s (error: %s)",
                event.getNode().getName(),
                event.getTrack().getInfo().getTitle(),
                event.getException().getMessage()
        ));
    }

    public void handleTrackStuck(TrackStuckEvent event){
        logger.debug(String.format(
                "[Lava-Listener] Node '%s': Track stuck -> %s (thresholdMs: %d)",
                event.getNode().getName(),
                event.getTrack().getInfo().getTitle(),
                event.getThresholdMs()
        ));
    }

    public void handleWebsocketClosed(WebSocketClosedEvent event){
        logger.debug(String.format(
                "[Lava-Listener] Node '%s': WebSocket closed (code: %d, reason: %s, byRemote: %b)",
                event.getNode().getName(),
                event.getCode(),
                event.getReason(),
                event.getByRemote()
        ));
    }


}
