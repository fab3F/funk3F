package net.fab3F.bot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.fab3F.Main;
import net.fab3F.bot.ServerCommand;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;


public class TestCommand implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getOption("title") == null || e.getMember() == null || e.getGuild() == null){
            return false;
        }

        Guild g = e.getGuild();

        // Join VC (or not)
        if(!VoiceHelper.joinHelper(e.getMember(), g.getSelfMember(), e.getJDA())){
            return false;
        }

        final String identifier = e.getOption("title").getAsString();

        Main.bot.getMusicHelper().addContent(identifier, e, false);

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.ADMIN;
    }

    @Override
    public PermissionGroup getBotPermission() {
        return PermissionGroup.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return """
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.
                Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.
                Der Bot muss Zugriff auf den Sprachkanal haben.""";
    }

    @Override
    public String getDescription() {
        return "Spiele einen Song oder eine Playlist ab";
    }

    @Override
    public Option[] getOptions() {
        return new ServerCommand.Option[]{new Option(OptionType.STRING, "title", "Der Name oder die URL des Songs oder eine Spotify/YouTube Playlist", true)};
    }

}
