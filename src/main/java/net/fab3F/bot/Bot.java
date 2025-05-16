package net.fab3F.bot;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.fab3F.Main;
import net.fab3F.bot.perm.PermissionWorker;
import net.fab3F.customTools.Logger;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {

    private final Logger logger;
    private final CommandManager commandManager;
    private ShardManager shardManager;

    public PermissionWorker pW;

    private long lastMusicPlayerManagerRestart;
    private ScheduledExecutorService schedulerService;
    private PlayerManager playerManager;

    public Bot(String token){
        this.logger = Main.main.getLogger();
        if(token==null || token.isBlank()){
            logger.error("No Discord Bot Token.");
            System.exit(0);
        }
        this.pW = new PermissionWorker();
        this.commandManager = new CommandManager();
        this.buildBot(token);



        lastMusicPlayerManagerRestart = System.currentTimeMillis();

    }

    private void buildBot(String token){
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(getActivity(Main.main.botConfig.getActivity()));
        builder.addEventListeners(new SlashCommandListener())
                .addEventListeners(new ChannelListener())
                .addEventListeners(new MessageListener());
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        builder.disableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES);
        builder.disableCache(CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS);
        this.shardManager = builder.build();
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
                    default -> Activity.watching(b);
                };
            }
        }
        return Activity.watching("My owner cannot configure this Bot.");
    }

}
