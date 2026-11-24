package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.fab3F.ConfigWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MusicHandler {
    private final Logger logger = LoggerFactory.getLogger(MusicHandler.class);
    private final ConfigWorker cw;
    private ShardManager sM = null;
    private final LavalinkClient client;
    private final HashMap<Long, GuildMusicHandler> handlers = new HashMap<>();

    public MusicHandler(long botId, ConfigWorker cw){
        this.cw = cw;
        LavaManager lavaManager = new LavaManager(botId, this, cw);
        this.client = lavaManager.getClient();
        new AutoplayLoader(this, cw);
    }

    public void setShardManager(ShardManager sM){
        this.sM = sM;
    }

    public GuildMusicHandler getOrCreateGuildMusicHandler(long guildId) {
        synchronized (this){
            Guild g = this.sM.getGuildById(guildId);
            if(g != null){
                return handlers.computeIfAbsent(guildId, handler -> new GuildMusicHandler(g, this.cw, this.client));
            }
            return null;
        }
    }

    public GuildMusicHandler getGuildMusicHandlerIfPresent(long guildId) {
        synchronized (this) {
            return handlers.get(guildId);
        }
    }

    //Checks if content can be added
    public boolean addContent(String input, SlashCommandInteractionEvent e, boolean playAsFirst){
        if(e.getGuild() == null)
            return false;

        e.deferReply().queue();


        // TODO Replace unallowed characters?

        logger.debug("Loading new input: {}", input);
        if(!input.startsWith("https")){
            input = "ytsearch:" + input + " audio";
        }

        Guild g = e.getGuild();
        final Link link = this.client.getOrCreateLink(g.getIdLong()); //exception ? what does this mean?

        link.loadItem(input).subscribe(new AudioLoader(input, e, this.getOrCreateGuildMusicHandler(g.getIdLong()).getScheduler(), playAsFirst));
        return true;
    }

    public void addAutoplayContent(String input, long guildId, long channelId){
        GuildMusicHandler musicHandler = this.getGuildMusicHandlerIfPresent(guildId);
        if(musicHandler == null) {
            logger.debug("Cancelled Autoplay-Loading of input \"{}\", because GuildMusicHandler was stopped while loading.", input);
            return;
        }

        logger.debug("Loading new Autoplay input: {}", input);
        if(!input.startsWith("https")){
            input = "ytsearch:" + input + " audio";
        }

        final Link link = this.client.getOrCreateLink(guildId);

        link.loadItem(input).subscribe(new AudioLoader(input, musicHandler.getScheduler(), channelId));
    }

    public boolean stopGuild(Guild guild){
        GuildMusicHandler guildMusicHandler;
        synchronized (this) {
            guildMusicHandler = this.handlers.remove(guild.getIdLong());
        }
        if (guildMusicHandler != null) {
            release(guildMusicHandler);
            return true;
        }
        // No handler but bot might be stuck in voice channel
        if (VoiceHelper.inVoiceChannel(guild.getSelfMember()) != null) {
            Link link = this.client.getLinkIfCached(guild.getIdLong());
            if (link != null) {
                link.destroy().subscribe();
            }
            guild.getJDA().getDirectAudioController().disconnect(guild);
            return true;
        }
        return false;
    }

    public int stopAll(Duration timeout){
        List<GuildMusicHandler> all;
        synchronized (this) {
            all = new ArrayList<>(handlers.values());
            handlers.clear();
        }
        CompletableFuture<?>[] futures = all.stream().map(this::release).toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(futures).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("Not all players were terminated in time.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.warn("Error while closing the players: {}", e.getMessage());
        }
        return all.size();
    }

    private CompletableFuture<Void> release(GuildMusicHandler handler) {
        Guild guild = handler.getGuild();
        handler.getScheduler().close();
        CompletableFuture<Void> destroyed = handler.getLink()
                .map(link -> link.destroy().toFuture().<Void>thenApply(ignored -> null))
                .orElseGet(() -> CompletableFuture.completedFuture(null))
                .exceptionally(err -> {
                    logger.warn("Player Destruction faield: {}", err.getMessage());
                    return null;
                });
        guild.getJDA().getDirectAudioController().disconnect(guild);
        return destroyed;
    }

    public void close() {
        this.client.close();
    }

    public LavalinkClient getClient(){
        return this.client;
    }

    public static String calcDuration(int millis){
        int durationInSeconds = millis / 1000;
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;
        String length;
        if (hours > 0) {
            length = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            length = String.format("%02d:%02d", minutes, seconds);
        }
        return length;
    }
}
