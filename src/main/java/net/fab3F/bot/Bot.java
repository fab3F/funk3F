package net.fab3F.bot;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.fab3F.ConfigWorker;
import net.fab3F.bot.listener.VoiceListener;
import net.fab3F.bot.listener.MessageListener;
import net.fab3F.bot.listener.SlashCommandListener;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.perm.PermissionWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Bot {

    private final Logger logger = LoggerFactory.getLogger(Bot.class);
    private ShardManager shardManager;
    private final ConfigWorker cw;
    private final CommandManager commandManager;
    private final PermissionWorker pW;
    private final MusicHandler musicHandler;
    private long lastMusicPlayerManagerRestart;

    public Bot(String token, ConfigWorker cw){
        if(token==null || token.isBlank()){
            logger.error("01: No Discord Bot Token.");
            System.exit(0);
        }
        this.cw = cw;
        this.pW = new PermissionWorker();

        long botId = Helpers.getUserIdFromToken(token);
        this.musicHandler = new MusicHandler(botId, cw);
        this.commandManager = new CommandManager(cw, pW, this.musicHandler);



        this.buildBot(token);
        this.musicHandler.setShardManager(shardManager);


        lastMusicPlayerManagerRestart = System.currentTimeMillis();

    }

    public MusicHandler getMusicHandler(){
        return this.musicHandler;
    }

    public void stop(){
        musicHandler.stopAll();
        shardManager.setStatus(OnlineStatus.OFFLINE);
        shardManager.shutdown();
    }

    public void reloadActivity(){
        this.shardManager.setActivity(getActivity(cw.getBotConfig().getActivity()));
    }



    private void buildBot(String token){
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.addEventListeners(new SlashCommandListener(commandManager))
                .addEventListeners(new VoiceListener())
                .addEventListeners(new MessageListener(cw, musicHandler));
        builder.setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(this.musicHandler.getClient()));
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
