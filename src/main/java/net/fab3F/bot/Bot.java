package net.fab3F.bot;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.fab3F.Main;
import net.fab3F.bot.listener.VoiceListener;
import net.fab3F.bot.listener.MessageListener;
import net.fab3F.bot.listener.SlashCommandListener;
import net.fab3F.bot.music.MusicHelper;
import net.fab3F.bot.perm.PermissionWorker;
import net.fab3F.customTools.Logger;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {

    private final Logger logger;
    public final CommandManager commandManager;
    private ShardManager shardManager;

    private final MusicHelper musicHelper;

    public PermissionWorker pW;

    private long lastMusicPlayerManagerRestart;
    private ScheduledExecutorService schedulerService;
    //private PlayerManager playerManager;

    public Bot(String token, Logger logger){
        this.logger = logger;
        if(token==null || token.isBlank()){
            logger.error("No Discord Bot Token.");
            System.exit(0);
        }
        this.pW = new PermissionWorker();
        this.commandManager = new CommandManager();
        long botId = Helpers.getUserIdFromToken(token);
        this.musicHelper = new MusicHelper(botId);




        this.buildBot(token);



        lastMusicPlayerManagerRestart = System.currentTimeMillis();

    }

    public MusicHelper getMusicHelper(){
        return this.musicHelper;
    }

    public void stop(){
        musicHelper.stopAll();
        shardManager.setStatus(OnlineStatus.OFFLINE);
        shardManager.shutdown();
    }

    public ShardManager getShardManager(){
        return shardManager;
    }

    public void reloadActivity(){
        this.shardManager.setActivity(getActivity(Main.botConfig.getActivity()));
    }



    private void buildBot(String token){
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.addEventListeners(new SlashCommandListener())
                .addEventListeners(new VoiceListener())
                .addEventListeners(new MessageListener());
        builder.setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(this.musicHelper.getClient()));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES);
        builder.enableCache(CacheFlag.VOICE_STATE);
        builder.disableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES);
        builder.disableCache(CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS);
        this.shardManager = builder.build();
        reloadActivity();
        this.logger.debug("Bot built!");
    }
    private Activity getActivity(List<String> activityL){
        String a, b;
        if(activityL.size() >= 2 && !activityL.get(0).isBlank() && !activityL.get(1).isBlank()){
            a = activityL.get(0);
            b = activityL.get(1);
            if(!a.isBlank() && !b.isBlank()){
                return switch (a) {
                    case "playing" -> Activity.playing(b);
                    case "listening" -> Activity.listening(b);
                    case "competing" -> Activity.competing(b);
                    case "watching" -> Activity.watching(b);
                    default -> Activity.customStatus(b);
                };
            }
        }
        return Activity.customStatus("My owner cannot configure this Bot.");
    }

}
