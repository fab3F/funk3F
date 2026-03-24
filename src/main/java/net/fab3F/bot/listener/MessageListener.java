package net.fab3F.bot.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fab3F.ConfigWorker;
import net.fab3F.Main;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final ConfigWorker cw;
    private final MusicHandler mH;

    public MessageListener(ConfigWorker cw, MusicHandler mH){
        this.cw = cw;
        this.mH = mH;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(cw.getBotConfig().getAdminIds().contains(event.getAuthor().getId())) {
            String msg = event.getMessage().getContentDisplay();
            String[] parts = msg.split(" ");
            if(parts.length <= 1)
                return;

            if (msg.startsWith("shutdown")) {
                logger.debug("Shutdown recieved: {}", msg);
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Shutdown!").queue();
                    Main.exit();
                }
            } else if (msg.startsWith("restart")) {
                logger.debug("Restart recieved: {}", msg);
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Restarting!").queue();
                    Main.restart();
                }
            } else if (msg.startsWith("reload")) {
                logger.debug("Reload recieved: {}", msg);
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Reloading Bot Config! Not everything (like bot token) will be reloaded and requires restart.").queue();
                    Main.reloadBotConfig();
                }
            } else if (msg.startsWith("autoplay3243423")) {
                logger.debug("Autoplay: {}", msg);
                VoiceHelper.joinHelper(event.getMember(), event.getGuild().getSelfMember(), event.getJDA(), true);
                mH.getOrCreateTrackScheduler(event.getGuild().getIdLong()).startAutoplay(event.getChannel().asTextChannel().getIdLong());
            } else if (msg.startsWith("sktip2134234")) {
                logger.debug("Skip: {}", msg);
                mH.getOrCreateTrackScheduler(event.getGuild().getIdLong()).skip();
            }
        }
    }

}
