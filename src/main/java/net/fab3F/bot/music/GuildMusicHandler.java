package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.fab3F.ConfigWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GuildMusicHandler {
    private final Logger logger = LoggerFactory.getLogger(GuildMusicHandler.class);
    private final LavalinkClient client;

    private final Guild guild;
    private final TrackScheduler scheduler;

    private int volume;


    public GuildMusicHandler(Guild guild, ConfigWorker cw, LavalinkClient client){
        this.guild = guild;
        this.client = client;
        final boolean isAutoplay = Boolean.parseBoolean(cw.getServerConfig(this.guild.getId()).get("default_autoplay"));
        this.scheduler = new TrackScheduler(this, isAutoplay);
        try{
            this.volume = Integer.parseInt(cw.getServerConfig(this.guild.getId()).get("default_volume"));
        } catch (NumberFormatException ex){
            this.volume = 35;
            logger.error("30: Volume-Konfiguration inkorrekt für: {}", this.guild.getId());
        }
    }

    public TrackScheduler getScheduler(){
        return this.scheduler;
    }

    public Guild getGuild(){
        return this.guild;
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(
                this.client.getLinkIfCached(this.guild.getIdLong())
        );
    }

    public void setVolume(int volume){
        this.volume = Math.min(Math.max(volume, 0), 100);
        this.getLink().ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setVolume(this.volume)
                        .subscribe()
        );
    }

    public int getVolume(){
        return this.volume;
    }


}
