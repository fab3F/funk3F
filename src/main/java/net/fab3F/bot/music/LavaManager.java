package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import net.fab3F.Main;
import net.fab3F.bot.listener.LavaListener;
import net.fab3F.customTools.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LavaManager {

    private final Logger logger;
    private final LavalinkClient client;
    private final Map<Class<? extends EmittedEvent>, Consumer<EmittedEvent>> handlers = new HashMap<>();
    private final LavaListener lavaListener;

    public LavaManager(long botId){
        this.logger = Main.logger;
        this.lavaListener = new LavaListener(this.logger);
        this.client = new LavalinkClient(botId);
        this.client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        this.registerLavalinkNode();
        this.registerLavalinkListeners();
    }

    public LavalinkClient getClient(){
        return client;
    }


    private void registerLavalinkNode() {
        NodeOptions options = new NodeOptions.Builder()
                .setName("funk3F-Node")
                .setServerUri(URI.create(String.format("ws://%s:%s",
                        Main.botConfig.getLavalinkAddress(),
                        Main.botConfig.getLavalinkPort())))
                .setPassword(Main.botConfig.getLavalinkPassword())
                .setRegionFilter(RegionGroup.EUROPE)
                .setHttpTimeout(5000L)
                .build();
        this.client.addNode(options);
    }

    private void registerLavalinkListeners() {
        this.client.on(dev.arbjerg.lavalink.client.event.ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();
            logger.log("[Lava-Listener] Node ready: " + node.getName() + " - Session id is: " + event.getSessionId());
        });

        this.client.on(StatsEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();
            logger.debug(String.format(
                    "[Lava-Listener] Node '%s' has stats: Current players: %d/%d",
                    node.getName(),
                    event.getPlayingPlayers(),
                    event.getPlayers()
            ));
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
                logger.debug(String.format(
                        "[Lava-Listener] Node '%s': Unhandled event -> %s",
                        nodeName,
                        event.getClass().getSimpleName()
                ));
            }
        });
    }

}
