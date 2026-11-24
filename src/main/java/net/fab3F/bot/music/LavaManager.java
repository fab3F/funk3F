package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.IRegionFilter;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import net.fab3F.ConfigWorker;
import net.fab3F.bot.listener.LavaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class LavaManager {

    private final Logger logger = LoggerFactory.getLogger(LavaManager.class);

    private final LavalinkClient client;
    private final Map<Class<? extends EmittedEvent>, Consumer<EmittedEvent>> handlers = new HashMap<>();
    private final LavaListener lavaListener;

    public LavaManager(long botId, MusicHandler mH, ConfigWorker cw){
        this.lavaListener = new LavaListener(mH);
        this.client = new LavalinkClient(botId);
        this.client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        this.registerLavalinkNodes(cw);
        this.registerLavalinkListeners();
    }

    public LavalinkClient getClient(){
        return client;
    }

    private void registerLavalinkNodes(ConfigWorker cw) {
        for (ConfigWorker.LavalinkNodeConfig node : cw.readBotConfig(false).getLavalinkNodes()) {
            NodeOptions.Builder b = new NodeOptions.Builder()
                    .setName(node.getName())
                    .setServerUri(URI.create("ws://" + node.getAddress() + ":" + node.getPort()))
                    .setPassword(node.getPassword())
                    .setHttpTimeout(5000L);
            IRegionFilter region = parseRegion(node.getRegion());
            if (region != null) {
                b.setRegionFilter(region);
            }
            this.client.addNode(b.build());
            logger.info("[Lava-Listener] Node registered: {} ({})", node.getName(), region);
        }
    }

    private IRegionFilter parseRegion(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RegionGroup.INSTANCE.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown Region '{}', Node without Region filter", value);
            return null;
        }
    }

    private void registerLavalinkListeners() {
        this.client.on(dev.arbjerg.lavalink.client.event.ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();
            logger.info("[Lava-Listener] Node ready: {} - Session id is: {}", node.getName(), event.getSessionId());

            if(!event.getResumed()){
                logger.warn("[Lava-Listener] Node {} had a hard reset. Resetting Players!", node.getName()); // Meldung wird aktuell auch beim start angezeigt
                // TODO reset einbauen

            }

        });

        this.client.on(StatsEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();
            logger.debug("[Lava-Listener] Node '{}' has stats: Current players: {}/{}", node.getName(), event.getPlayingPlayers(), event.getPlayers());
        });

        handlers.put(TrackStartEvent.class, e -> lavaListener.handleTrackStart((TrackStartEvent) e));
        handlers.put(TrackEndEvent.class, e -> lavaListener.handleTrackEnd((TrackEndEvent) e));
        handlers.put(TrackExceptionEvent.class, e -> lavaListener.handleTrackException((TrackExceptionEvent) e));
        handlers.put(TrackStuckEvent.class, e -> lavaListener.handleTrackStuck((TrackStuckEvent) e));
        handlers.put(WebSocketClosedEvent.class, e -> lavaListener.handleWebsocketClosed((WebSocketClosedEvent) e));

        this.client.on(EmittedEvent.class).subscribe(event -> {
            final LavalinkNode node = event.getNode();
            final String nodeName = node.getName();

            var handler = handlers.get(event.getClass());
            if (handler != null) {
                handler.accept(event);
            } else {
                logger.debug("[Lava-Listener] Node '{}': Unhandled event -> {}", nodeName, event.getClass().getSimpleName());
            }
        });
    }

}
