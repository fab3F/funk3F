package net.fab3F.bot.commands.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.fab3F.bot.ServerCommand;
import net.fab3F.bot.ServerMusicCommand;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;

import java.util.Objects;


public class PlayMusicCmd extends ServerMusicCommand {
    public PlayMusicCmd(MusicHandler musicHandler) {
        super(musicHandler);
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getOption("title") == null || e.getGuild() == null){
            return false;
        }

        // Join VC
        if(!VoiceHelper.joinHelper(e.getMember(), e.getGuild().getSelfMember(), e.getJDA(), true)){
            return false;
        }

        final String identifier = Objects.requireNonNull(e.getOption("title")).getAsString();

        return musicHandler.addContent(identifier, e, false);
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.ADMIN;
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
