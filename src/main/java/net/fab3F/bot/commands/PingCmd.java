package net.fab3F.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fab3F.bot.ServerCommand;
import net.fab3F.bot.perm.PermissionGroup;

public class PingCmd implements ServerCommand {


    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.TEXT_NORMAL;
    }

    @Override
    public PermissionGroup getBotPermission() {
        return PermissionGroup.BOT_TEXT;
    }

    @Override
    public String getDescription() {
        return "Berechne den Ping des Bots";
    }

    @Override
    public String cmdName() {
        return "ping";
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.getChannel().sendTyping().queue(v -> {
            long ping = System.currentTimeMillis() - time;
            event.reply("Pong! <:table_tennis:944546187724345454> Der Ping des Bots beträgt `" + ping + "ms`!").queue();
        });
        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return false;
    }
}
