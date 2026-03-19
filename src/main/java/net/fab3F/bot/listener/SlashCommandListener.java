package net.fab3F.bot.listener;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.fab3F.bot.CommandManager;


public class SlashCommandListener extends ListenerAdapter {

    private final CommandManager cm;

    public SlashCommandListener(CommandManager cm){
        this.cm = cm;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
       cm.perform(event);
    }

    @Override
    public void onReady(ReadyEvent event) {
        cm.updateCommands(event.getJDA());
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
