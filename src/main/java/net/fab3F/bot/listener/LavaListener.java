package net.fab3F.bot.listener;

import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fab3F.bot.music.CustomTrackData;
import net.fab3F.bot.music.GuildMusicHandler;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.TrackScheduler;
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
        GuildMusicHandler guildMusicHandler = mH.getGuildMusicHandlerIfPresent(event.getGuildId());
        if(guildMusicHandler == null) {
            return;
        }
        TrackScheduler scheduler = guildMusicHandler.getScheduler();
        if(!scheduler.onTrackStart(track)) {
            return;
        }

        Guild guild = scheduler.getGuild();
        logger.debug("[Lava-Listener] Node: '{}'; Guild: '{} ({})' ; Track started -> {}", event.getNode().getName(), guild.getName(), guild.getId(), track.getInfo().getTitle());

        TrackInfo info = track.getInfo();
        String msg = "Jetzt spielt: **`" + info.getTitle() + "`** von **`" + info.getAuthor() + "`** [" + MusicHandler.calcDuration((int)info.getLength()) + "]";
        TextChannel channel = guild.getChannelById(TextChannel.class, track.getUserData(CustomTrackData.class).channelId());
        if (channel != null) {
            channel.sendMessage(msg).queue();
        }
    }

    public void handleTrackEnd(TrackEndEvent event){
        logger.debug("[Lava-Listener] Node '{}': Track ended -> {} (reason: {})", event.getNode().getName(), event.getTrack().getInfo().getTitle(), event.getEndReason());
        GuildMusicHandler guildMusicHandler = mH.getGuildMusicHandlerIfPresent(event.getGuildId());
        if(guildMusicHandler == null) {
            return;
        }
        guildMusicHandler.getScheduler().onTrackEnd(event.getTrack(), event.getEndReason().name(), event.getEndReason().getMayStartNext());
    }

    public void handleTrackException(TrackExceptionEvent event){
        logger.warn("[Lava-Listener] Node '{}': Track exception -> {} (error: {})", event.getNode().getName(), event.getTrack().getInfo().getTitle(), event.getException().getMessage());

        // TODO: Play next song or stop
    }

    public void handleTrackStuck(TrackStuckEvent event){
        logger.debug("[Lava-Listener] Node '{}': Track stuck -> {} (thresholdMs: {})", event.getNode().getName(), event.getTrack().getInfo().getTitle(), event.getThresholdMs());
        GuildMusicHandler h = mH.getGuildMusicHandlerIfPresent(event.getGuildId());
        if (h != null) {
            h.getScheduler().skip();
        }
    }

    public void handleWebsocketClosed(WebSocketClosedEvent event){
        logger.debug("[Lava-Listener] Node '{}': WebSocket closed (code: {}, reason: {}, byRemote: {})", event.getNode().getName(), event.getCode(), event.getReason(), event.getByRemote());

        // TODO: stop
    }


}
