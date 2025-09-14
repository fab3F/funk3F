package net.fab3F.bot.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fab3F.Main;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(Main.botConfig.getAdminIds().contains(event.getAuthor().getId())) {
            String msg = event.getMessage().getContentDisplay();
            String[] parts = msg.split(" ");
            if(parts.length <= 1)
                return;

            if (msg.startsWith("shutdown")) {
                Main.logger.debug("Shutdown recieved: " + msg);
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Shutdown!").queue();
                    Main.exit();
                }
            } else if (msg.startsWith("restart")) {
                Main.logger.debug("Restart recieved: " + msg);
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Restarting!").queue();
                    Main.restart();
                }
            } else if (msg.startsWith("reload")) {
                Main.logger.debug("Reload recieved: " + msg);
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Reloading Bot Config! Not everything (like bot token) will be reloaded and requires restart.").queue();
                    Main.reloadBotConfig();
                }
            }
        }
    }

}
