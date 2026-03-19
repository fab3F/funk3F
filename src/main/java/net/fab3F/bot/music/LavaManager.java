package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import net.fab3F.ConfigWorker;
import net.fab3F.bot.listener.LavaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LavaManager {

    private final Logger logger = LoggerFactory.getLogger(LavaManager.class);
    private final MusicHandler musicHandler;

    private final LavalinkClient client;
    private final Map<Class<? extends EmittedEvent>, Consumer<EmittedEvent>> handlers = new HashMap<>();
    private final LavaListener lavaListener;

    public LavaManager(long botId, MusicHandler mH){
        this.musicHandler = mH;
        this.lavaListener = new LavaListener(mH);
        this.client = new LavalinkClient(botId);
        this.client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        this.registerLavalinkNode();
        this.registerLavalinkListeners();
    }

    public LavalinkClient getClient(){
        return client;
    }


    private void registerLavalinkNode() {
        ConfigWorker cw = this.musicHandler.getConfigWorker();
        NodeOptions options = new NodeOptions.Builder()
                .setName("funk3F-Node")
                .setServerUri(URI.create(String.format("ws://%s:%s",
                        cw.getBotConfig().getLavalinkAddress(),
                        cw.getBotConfig().getLavalinkPort())))
                .setPassword(cw.getBotConfig().getLavalinkPassword())
                .setRegionFilter(RegionGroup.EUROPE)
                .setHttpTimeout(5000L)
                .build();
        this.client.addNode(options);
    }

    private void registerLavalinkListeners() {
        this.client.on(dev.arbjerg.lavalink.client.event.ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();
            logger.info("[Lava-Listener] Node ready: {} - Session id is: {}", node.getName(), event.getSessionId());
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
