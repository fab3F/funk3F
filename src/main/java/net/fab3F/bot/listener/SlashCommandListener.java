package net.fab3F.bot.listener;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.fab3F.Main;


public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
       Main.bot.commandManager.perform(event);
    }

    @Override
    public void onReady(ReadyEvent event) {
        Main.bot.commandManager.updateCommands(event.getJDA());
    }

    @Override
    public void onGuildReady(GuildReadyEvent event){

        event.getGuild().retrieveCommands().queue(guildCommands -> {
            for (Command guildCommand : guildCommands) {
                event.getGuild().deleteCommandById(guildCommand.getId()).queue();
            }
        });

    }

}
