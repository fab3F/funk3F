package net.fab3F.bot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fab3F.bot.ServerMusicCommand;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;

public class StopMusicCmd extends ServerMusicCommand {
    public StopMusicCmd(MusicHandler musicHandler) {
        super(musicHandler);
    }

    @Override
    public boolean performCommand(SlashCommandInteractionEvent e) {
        if(e.getGuild() == null){
            return false;
        }

        // Check for same voice channel
        if(!VoiceHelper.joinHelper(e.getMember(), e.getGuild().getSelfMember(), e.getJDA(), false)){
            return false;
        }

        if(this.musicHandler.stopGuild(e.getGuild())){
            e.reply("Wiedergabe gestoppt und Wiedergabeliste geleert.").queue();
            return true;
        }
        return false;
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.VOICE_NORMAL;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.";
    }

    @Override
    public String getDescription() {
        return "Stoppt die Musik und löscht die Wiedergabeliste";
    }
}
