package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.fab3F.ConfigWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Optional;

public class MusicHandler {
    private final Logger logger = LoggerFactory.getLogger(MusicHandler.class);
    private ShardManager sM;
    private final ConfigWorker cw;
    private final LavalinkClient client;
    private final HashMap<Long, TrackScheduler> schedulers = new HashMap<>();
    public final AutoplayLoader autoplayLoader;

    public MusicHandler(long botId, ConfigWorker cw){
        this.cw = cw;
        LavaManager lavaManager = new LavaManager(botId, this);
        this.client = lavaManager.getClient();
        this.autoplayLoader = new AutoplayLoader(this);

    }

    public void setShardManager(ShardManager sM){
        this.sM = sM;
    }

    public ShardManager getShardManager(){
        return sM;
    }

    public ConfigWorker getConfigWorker(){
        return cw;
    }

    public void addContent(String input, SlashCommandInteractionEvent e, boolean playAsFirst){
        if(e.getGuild() == null)
            return;

        e.deferReply().queue();

        // Replace unallowed characters?

        logger.debug("Loading new input: {}", input);
        if(!input.startsWith("https")){
            input = "ytsearch:" + input + " audio";
        }

        final Link link = this.client.getOrCreateLink(e.getGuild().getIdLong()); //exception

        link.loadItem(input).subscribe(new AudioLoader(input, e, this.getOrCreateTrackScheduler(e.getGuild().getIdLong()), playAsFirst));

    }

    public void addAutoplayContent(String input, long guildId, long channelId){
        logger.debug("Loading new Autoplay input: {}", input);
        if(!input.startsWith("https")){
            input = "ytsearch:" + input + " audio";
        }

        final Link link = this.client.getOrCreateLink(guildId);

        link.loadItem(input).subscribe(new AudioLoaderAutoplay(input, this.getOrCreateTrackScheduler(guildId), channelId));
    }


    public void stopAll(){

    }

    public Optional<Link> getLink(long guildId) {
        return Optional.ofNullable(
                this.client.getLinkIfCached(guildId)
        );
    }

    public Optional<LavalinkPlayer> getPlayer(long guildId) {
        return this.getLink(guildId).map(Link::getCachedPlayer);
    }


    public Link getLink2(long guildId) {
        return this.client.getOrCreateLink(guildId);
    }

    public Mono<LavalinkPlayer> getPlayer2(long guildId) {
        return this.getLink2(guildId).getPlayer();
    }

    public LavalinkClient getClient(){
        return this.client;
    }

    public TrackScheduler getOrCreateTrackScheduler(long guildId){
        return schedulers.computeIfAbsent(guildId, scheduler -> new TrackScheduler(guildId,this));
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

    public TextChannel resolveTextChannel(long guildId, long channelId) {
        Guild guild = sM.getGuildById(guildId);
        if (guild == null) return null;
        return guild.getChannelById(TextChannel.class, channelId);
    }

}
