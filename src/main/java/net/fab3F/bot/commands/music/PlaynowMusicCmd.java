package net.fab3F.bot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.fab3F.bot.ServerMusicCommand;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;

import java.util.Objects;


public class PlaynowMusicCmd extends ServerMusicCommand {
    public PlaynowMusicCmd(MusicHandler musicHandler) {
        super(musicHandler);
    }

    @Override
    public boolean performCommand(SlashCommandInteractionEvent e) {
        if(e.getOption("title") == null || e.getGuild() == null){
            return false;
        }

        // Join VC (or not)
        if(!VoiceHelper.joinHelper(e.getMember(), e.getGuild().getSelfMember(), e.getJDA(), true)){
            return false;
        }

        final String identifier = Objects.requireNonNull(e.getOption("title")).getAsString();

        return musicHandler.addContent(identifier, e, true);
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.VOICE_ADVANCED;
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
        return "Spiele einen Song sofort als nächsten ab (Nicht für Playlists verfügbar)";
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{new Option(OptionType.STRING, "title", "Der Name oder die URL des Songs oder eine Spotify/YouTube Playlist", true)};
    }

}
