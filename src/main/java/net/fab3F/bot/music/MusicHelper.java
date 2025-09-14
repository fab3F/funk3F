package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fab3F.Main;
import net.fab3F.customTools.Logger;

import java.util.HashMap;
import java.util.Optional;

public class MusicHelper {
    private final Logger logger;
    private final LavalinkClient client;
    private final HashMap<Long, TrackScheduler> schedulers = new HashMap<>();

    public MusicHelper(long botId){
        this.logger = Main.logger;
        LavaManager lavaManager = new LavaManager(botId);
        this.client = lavaManager.getClient();


    }

    public void addContent(String input, SlashCommandInteractionEvent e, boolean playAsFirst){
        if(e.getGuild() == null)
            return;

        e.deferReply().queue();

        // Replace unallowed characters?

        logger.debug("Loading new input: " + input);
        if(!input.startsWith("https")){
            input = "ytsearch:" + input + " audio";
        }

        final Link link = this.client.getOrCreateLink(e.getGuild().getIdLong());

        link.loadItem(input).subscribe(new AudioLoader(input, e, this.getOrCreateTrackScheduler(e.getGuild().getIdLong())));

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

    private TrackScheduler getOrCreateTrackScheduler(long guildId){
        return schedulers.computeIfAbsent(guildId, TrackScheduler::new);
    }
}
