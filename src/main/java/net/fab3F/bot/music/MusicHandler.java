package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.fab3F.ConfigWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    public GuildMusicHandler getOrCreateGuildMusicHandler(long guildId){
        synchronized (this){
            Guild g = this.sM.getGuildById(guildId);
            if(g != null){
                return handlers.computeIfAbsent(guildId, handler -> new GuildMusicHandler(g, this.cw, this.client));
            }
            return null;
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
        logger.debug("Loading new Autoplay input: {}", input);
        if(!input.startsWith("https")){
            input = "ytsearch:" + input + " audio";
        }

        final Link link = this.client.getOrCreateLink(guildId);

        link.loadItem(input).subscribe(new AudioLoader(input, this.getOrCreateGuildMusicHandler(guildId).getScheduler(), channelId));
    }

    public boolean stopGuild(Guild guild){
        long id = guild.getIdLong();
        if(!this.handlers.containsKey(id)){
            return false;
        }

        this.handlers.get(id).getLink().ifPresent(
                (link) -> link.destroy().subscribe()
        );

        guild.getJDA().getDirectAudioController().disconnect(guild);

        this.handlers.remove(id);
        return true;
    }

    public void stopAll(DirectAudioController audioController){
        Iterator<Map.Entry<Long, GuildMusicHandler>> iterator = this.handlers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, GuildMusicHandler> entry = iterator.next();
            GuildMusicHandler handler = entry.getValue();
            Long id = entry.getKey();
            if (handler != null) {
                handler.getLink().ifPresent(
                        (link) -> link.destroy().subscribe()
                );
                audioController.disconnect(this.sM.getGuildById(id));
                this.handlers.remove(id);
            }
            iterator.remove();
        }
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
